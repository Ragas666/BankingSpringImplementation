package com.rab3tech.customer.employee.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rab3tech.customer.service.CustomerService;
import com.rab3tech.customer.service.impl.CustomerEnquiryService;
import com.rab3tech.email.service.EmailService;
import com.rab3tech.utils.BankHttpUtils;
import com.rab3tech.vo.CustomerAccountInfoVO;
import com.rab3tech.vo.CustomerSavingVO;
import com.rab3tech.vo.CustomerVO;
import com.rab3tech.vo.EmailVO;

@Controller
public class EmployeeUIController {
	
    private static final Logger logger = LoggerFactory.getLogger(EmployeeUIController.class);
	
	@Autowired
	private CustomerEnquiryService customerEnquiryService;
	
	@Autowired
	private CustomerService customerService;
	
	@Value("${customer.registration.url}")
	private String registrationURL;
	
	@Autowired
	private EmailService emailService;
	
	

	 
	
	@GetMapping("/employee/customersList")
	public String showCustomerList(Model model) {
	   List<CustomerVO> customerVOs=customerService.findCustomers();
	   model.addAttribute("customerVOs", customerVOs);
	   return "employee/customersList";
	}
	
	@GetMapping(value= {"/asc"})
	  
		public String showAscOrder(Model model) {
			
			List<CustomerSavingVO> ascending = customerEnquiryService.sortAscOrder();
			model.addAttribute("applicants", ascending);
			return "employee/customerEnquiryList";	
			}
	
	
	@GetMapping("/customer/lock")
	public String customerLock(@RequestParam String userid) {
	   customerService.updateCustomerLockStatus(userid, "yes");
	   return "redirect:/employee/customers";
	}
	
	@GetMapping("/customer/unlock")
	public String customerUnlock(@RequestParam String userid) {
	   customerService.updateCustomerLockStatus(userid, "no");
	   return "redirect:/employee/customers";
	}
	
	@GetMapping("/employee/customers")
	public String showCustomer(Model model) {
	   List<CustomerVO> customerVOs=customerService.findCustomers();
	   model.addAttribute("customerVOs", customerVOs);
	   return "employee/customers";
	}
	
	@PreAuthorize("hasAuthority('EMPLOYEE')")
	@GetMapping("/customers/account/approve")
	public String customerAccountApproveGet(@RequestParam int csaid) {
		CustomerAccountInfoVO accountInfoVO=customerService.createBankAccount(csaid);
		System.out.println(accountInfoVO);
		return "redirect:/customer/accounts/approved";
	}
	
	@PreAuthorize("hasAuthority('EMPLOYEE')")
	@PostMapping("/customers/account/approve")
	public String customerAccountApprove(@RequestParam int csaid) {
		CustomerAccountInfoVO accountInfoVO=customerService.createBankAccount(csaid);
		System.out.println(accountInfoVO);
		return "redirect:/customer/accounts/approved";
	}
	
	//This is showing customers who are already registered and account is not created so far
	@GetMapping(value= {"/customer/accounts/approved"})
    @PreAuthorize("hasAuthority('EMPLOYEE')")
	public String showCustomerAccountsApproved(Model model) {
		logger.info("showCustomerAccountsApproved is called!!!");
		List<CustomerSavingVO> pendingApplications = customerEnquiryService.findRegisteredEnquiry();
		model.addAttribute("applicants", pendingApplications);
		return "employee/customerAccountsApproved";	//login.html
	}
	
	
	@GetMapping(value= {"/customer/enquiries"})
    @PreAuthorize("hasAuthority('EMPLOYEE')")
	public String showCustomerEnquiry(Model model) {
		logger.info("showCustomerEnquiry is called!!!");
		List<CustomerSavingVO> pendingApplications = customerEnquiryService.findPendingEnquiry();
		model.addAttribute("applicants", pendingApplications);
		return "employee/customerEnquiryList";	//login.html
	}
	
	@PostMapping("/customers/enquiry/approve")
	public String customerEnquiryApprove(@RequestParam int csaid,HttpServletRequest request) {
		CustomerSavingVO customerSavingVO=customerEnquiryService.changeEnquiryStatus(csaid, "APPROVED");
		String cuuid=BankHttpUtils.generateToken();
		customerEnquiryService.updateEnquiryRegId(csaid, cuuid);
		String registrationLink=BankHttpUtils.getServerBaseURL(request)+"/"+registrationURL+cuuid;
		//String registrationLink ="http://localhost:8080/v3/customer/registration/complete";
		EmailVO mail=new EmailVO(customerSavingVO.getEmail(),"javahunk2020@gmail.com","Regarding Customer "+customerSavingVO.getName()+"  Account registration","",customerSavingVO.getName());
		mail.setRegistrationlink(registrationLink);
		emailService.sendRegistrationEmail(mail);
		return "redirect:/customer/enquiries";
	}


	
	  @GetMapping("/customer/deleteCustomer") 
	  public String deleteCustomer(@RequestParam String userid,Model model) {
	  customerService.deleteCustomer(userid);
	  return "redirect:/employee/customersList"; }
}
