package com.validate.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.validate.dao.ContactRepository;
import com.validate.dao.MyOrderRepository;
import com.validate.dao.UserRepository;
import com.validate.entitie.Contact;
import com.validate.entitie.MyOrder;
import com.validate.entitie.User;
import com.validate.helper.Message;

import jakarta.servlet.http.HttpSession;
import com.razorpay.*;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bcryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	@Autowired
	private MyOrderRepository myOrderRepository;
	
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);

	}

	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	// add form contact
	@RequestMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	// add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			// processing and uploding file
			if (file.isEmpty()) {
				contact.setImage("default.png");
			} else {
				contact.setImage(file.getOriginalFilename());
				File savefile = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is Uploaded");
			}

			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			System.out.println("DATA: " + contact);
			session.setAttribute("message", new Message("Your contact is added !! Add more", "success"));
		} catch (Exception r) {
			System.out.println(r);
			r.printStackTrace();
			session.setAttribute("message", new Message("Something Went Wrong!! Try Again..", "danger"));
			// error message

		}
		return "normal/add_contact_form";
	}

	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
		String name = principal.getName();
		User user = userRepository.getUserByUserName(name);
		Pageable pageable = PageRequest.of(page, 8);
		int id = user.getId();
		Page<Contact> contacts = this.contactRepository.findContactsByUser(id, pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		m.addAttribute("title", "Show User Contacts");
		return "normal/show_contacts";
	}

	@RequestMapping("/contact/{cid}")
	public String showContactDetail(@PathVariable("cid") Integer cid, Model model, Principal principal) {
		System.out.println(cid);
		Optional<Contact> contact = contactRepository.findById(cid);
		Contact cont = contact.get();
		User user = userRepository.getUserByUserName(principal.getName());

		if (user.getId() == cont.getUser().getId()) {
			model.addAttribute("contact", cont);
			model.addAttribute("title", cont.getName());
		} else {
			System.out.println("YE NI CHALRA");
		}
		return "normal/contact_detail";
	}

	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, Model model, Principal principal,
			HttpSession session) {
		
		Contact contact=contactRepository.findById(cid).get();
		// check..
		User user = userRepository.getUserByUserName(principal.getName());
          user.getContacts().remove(contact);
					// contact.getImage();
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Contact deleted successfully", "success"));
		

		return "redirect:/user/show-contacts/0";
	}

	@PostMapping("/update-contact/{cid}")
	public String updateContact(@PathVariable("cid") Integer cid, Model m) {
		m.addAttribute("title", "Update Contact");
		Contact contact = this.contactRepository.findById(cid).get();
		m.addAttribute("contact", contact);
		return "normal/update_form";
	}

	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model m, HttpSession session,Principal principal) {
		try {
			System.out.println(contact.getCid());
			Contact oldcontact=contactRepository.findById(contact.getCid()).get();
			if(!file.isEmpty())
			{
				//delete old photo
				File deletefile = new ClassPathResource("static/image").getFile();
				File file1=new File(deletefile,oldcontact.getImage());
				file1.delete();
				
				
				
				//update new photo
				File savefile = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
				
				
				
				
			}
			else {
				contact.setImage(oldcontact.getImage());
			}
			User user=userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			contactRepository.save(contact);
			session.setAttribute("message",new Message("Your contact is updated...","success"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/user/contact/"+contact.getCid();
	}
	@GetMapping("/profile")
	public String yourProfile(Model model)
	{
		model.addAttribute("title","Profile DashBoard");
		return "normal/profile";
	}
	
	@GetMapping("/settings")
	public String openSettings() {
		return "normal/settings";
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPass,@RequestParam("newPassword") String newPass,Principal principal,HttpSession session) {
		String name=principal.getName();
		User user=userRepository.getUserByUserName(name);
	     System.out.print(oldPass);
		if(this.bcryptPasswordEncoder.matches(oldPass,user.getPassword()))
		{
			//change the password
			user.setPassword(bcryptPasswordEncoder.encode(newPass));
			this.userRepository.save(user);
			session.setAttribute("message",new Message("Your password is successfully changed...","success"));
			return "redirect:/user/index";
		}
		else
		{
			session.setAttribute("message",new Message("Please Enter correct old password !!","danger"));
			return "redirect:/user/settings";
		}
		
	}
	
	//creating order for payment
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String,Object> data,Principal principal) throws Exception{
		
		int amt=Integer.parseInt(data.get("amount").toString());
		RazorpayClient razorpayClient = new RazorpayClient("rzp_test_x2xzIAPvqWGOVm", "GJHNSh44py3JAo3582Chet9y");
		JSONObject ob=new JSONObject();
		ob.put("amount",amt*100);
		ob.put("currency","INR");
		ob.put("receipt","txn_235425");
		Order order=razorpayClient.Orders.create(ob);
		//save the order in database
		MyOrder myOrder=new MyOrder();
		myOrder.setAmount(order.get("amount")+"");
		myOrder.setOrderId(order.get("id"));
		myOrder.setPaymentId(null);
		myOrder.setStatus("created");
		myOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
		myOrder.setReceipt(order.get("receipt"));
		myOrderRepository.save(myOrder);
		
		System.out.println(data);
		System.out.println(order);
		return order.toString();
	}
	
	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data){
		
		MyOrder myOrder= myOrderRepository.findByOrderId(data.get("order_id").toString());
		myOrder.setPaymentId(data.get("payment_id").toString());
		myOrder.setStatus(data.get("status").toString());	
		myOrderRepository.save(myOrder);
		System.out.println(data);
		return ResponseEntity.ok(Map.of("msg","updated"));
	}
	
}
