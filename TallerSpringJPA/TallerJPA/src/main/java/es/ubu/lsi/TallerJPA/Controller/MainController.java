package es.ubu.lsi.TallerJPA.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.ubu.lsi.TallerJPA.Model.User;
import es.ubu.lsi.TallerJPA.Services.databaseService;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {
	
	@Autowired
	private databaseService databaseService;
	
	@GetMapping("/")
	public String mainPage(HttpSession session, Model model) {
		
		if (session.getAttribute("email") != null) {
			model.addAttribute("logued", true);
		}
		return "home";
	}
	
	
	@GetMapping("/login")
	public String login() {
		return "login";
	}
	
	@PostMapping("/checkLogin")
	public String checkLogin(Model model,  HttpSession session, @RequestParam("email") String email
			,@RequestParam("password") String password) {
		
		if (databaseService.checkRegistered(email, password)) {
			session.setAttribute("email", email);
			session.setAttribute("password", password);
			model.addAttribute("logued", true);
			return "home";
		}

		
		model.addAttribute("nouser", true);
		return "login";
	}
	
	/**
	 * Método getProfile.
	 * @param model the model 
	 * @param session the session
	 * @return profile view
	 */
	@GetMapping("/profile")
	public String getProfile(Model model, HttpSession session) {
		
		User user = databaseService.getUser((String) session.getAttribute("email"));
		
		model.addAttribute("user", user);
		
		return "profile";

	}
	
	/**
	 * Método getProfile.
	 * @param model the model 
	 * @param session the session
	 * @return profile view
	 */
	@PostMapping("/updateProfile")
	public String updateProfile(Model model, @RequestParam("email") String email, @RequestParam("password") String password
			,@RequestParam("firstname") String firstname, @RequestParam("lastname") String lastname) {
		
		databaseService.updateProfile(email, password, firstname, lastname);
		
		User user = databaseService.getUser(email);
		
		model.addAttribute("user", user);
		
		
		return "profile";

	}
	
	/**
	 * Método getProfile.
	 * @param model the model 
	 * @param session the session
	 * @return profile view
	 */
	@GetMapping("/showUsers")
	public String updateProfile(Model model) {
		
		List<User> userList = databaseService.getAllUsers();
				
		model.addAttribute("userList", userList);
		
		return "showUsers";

	}
	


}
