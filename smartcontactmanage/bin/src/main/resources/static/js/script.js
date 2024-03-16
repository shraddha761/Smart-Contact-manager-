console.log("this is script file");

/*const toggleSidebar=()=>{
	if($(".sidebar").is(":visible"))
	{
	  $(".sidebar").css("display","none");
	  $(".content").css("margin-left","0%"); 
	}
	else{
		$(".sidebar").css("display","block");
		$(".content").css("margin-left","20%"); 
	}
};*/

function toggleSidebar() {

	const sidebar = document.getElementsByClassName("sidebar")[0];
	const content = document.getElementsByClassName("content")[0];

	if (window.getComputedStyle(sidebar).display === "none") {
		sidebar.style.display = "block";
		content.style.marginLeft = "20%";
	}
	else {
		sidebar.style.display = "none";
		content.style.marginLeft = "0%";
	}
}

const search = () => {
	let query = $("#search-input").val();
	console.log(query);

	if (query == "") {
		$(".search-input").hide();
	}
	else {
		console.log(query);

		let url = `http://localhost:8080/search/${query}`;

		fetch(url)
			.then((response) => {
				return response.json();
			})
			.then((data) => {
				console.log(data);

				let text = `<div class='list-group'>`;

				data.forEach((contact) => {
					text += `<a href='/user/contact/${contact.cid}' class='list-group-item list-group-item-action'> ${contact.name + " " + contact.secondName} </a>`;
				});

				text += `</div>`;
				$(".search-result").html(text);
				$(".search-result").show();

			});

		$(".search-result").show();
		
	}
}


//first request to server to create order

const paymentStart = () => {
	console.log("Payment start");
	let amount = $("#payment_feild").val();
	console.log(amount);
	if (amount == '' || amount == null) {
		//  alert("Amount is required !!");
		swal("Failed !", "Amount is required !!", "error");
		return;
	}

	$.ajax(
		{
			url: '/user/create_order',
			data: JSON.stringify({ amount: amount, info: 'order_request' }),
			contentType: 'application/json',
			type: 'POST',
			dataType: 'json',
			success: function(response) {
				//invoked where success
				if (response.status == "created") {
					//open payment form
					let options = {
						key: "rzp_test_fdqGGq9sdnfVCH",
						amount: response.amount,
						currency: "INR",
						name: "SMART CONTACT MANAGER",
						description: "Donation",
						image: "https://qph.cf2.quoracdn.net/main-thumb-369997769-200-ecaaclonqidzbhcugtqnvaphbnepwefy.jpeg",
						order_id: response.id,
						handler: function(response) {
							console.log(response.razorpay_payment_id);
							console.log(response.razorpay_order_id);
							console.log(response.razorpay_signature);
							console.log("Payment successfull");
							updatePaymentOnServer(response.razorpay_payment_id, response.razorpay_order_id, "paid");
							swal("Good job!", "Payment successfull!", "success");
						},
						prefill: {
							name: "",
							email: "",
							contact: ""
						},
						notes: {
							address: "NAYAN BIRLA",
						},
						theme: {
							color: "#3399cc",
						},
					};

					let rzp = new Razorpay(options);
					rzp.on('payment.failed', function(response) {
						console.log(response.error.code);
						console.log(response.error.description);
						console.log(response.error.source);
						console.log(response.error.step);
						console.log(response.error.reason);
						console.log(response.error.metadata.order_id);
						console.log(response.error.metadata.payment_id);
						// alert("Oops payment failed");

						swal("Failed !", "payment failed!", "error");
					});
					rzp.open();
				}
			},
			error: function(error) {
				//invoke when error
				console.log(error)
				alert("Something went Wrong !!")
			}
		});
};

// 
function updatePaymentOnServer(payment_id, order_id, status) {
	$.ajax({
		url: '/user/update_order',
		data: JSON.stringify({ payment_id: payment_id, order_id: order_id, status: status }),
		contentType: 'application/json',
		type: 'POST',
		dataType: 'json',
		success: function(response) {
			swal("Good job!", "Payment successfull!", "success");
		},
		error: function() {
			console.log(error);
			swal("Failed !", "payment successfull, but we won't catch it, We will contact you soon!", "error");
		}
	})
}

