package com.atif.jobportal;

import com.atif.jobportal.entity.User;
//import com.atif.jobportal.repository.UserRepository;
import com.atif.jobportal.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JobprotalApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobprotalApplication.class, args);
	}

//	@Bean
//    CommandLineRunner run(UserRepository userRepository) {
//		return args -> {
//
//			User user = new User(
//					"Atif",
//					"atif@gmail.com",
//					"123456",
//					"RECRUITER"
//			);
//
//			userRepository.save(user);
//
//			System.out.println("User saved successfully!");
//		};
//	}

//	@Bean
//	CommandLineRunner run(UserService userService) {
//		return args -> {
//
//			User user = new User(
//					"Test User",
//					"test@gmail.com",
//					"123456",
//					"JOB_SEEKER"
//			);
//
//			userService.createUser(user);
//
//			System.out.println("User saved successfully!");
//		};
//	}

}
