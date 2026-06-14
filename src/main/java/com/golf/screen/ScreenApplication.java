package com.golf.screen;

import com.golf.screen.entity.User;
import com.golf.screen.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.util.List;

@SpringBootApplication
public class ScreenApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScreenApplication.class, args);
	}

	@Bean
	public CommandLineRunner initializeMannerTemperature(UserRepository userRepository) {
		return args -> {
			List<User> users = userRepository.findAll();
			for (User user : users) {
				if (user.getMannerTemperature() == null || user.getMannerTemperature() == 0.0) {
					user.setMannerTemperature(36.5);
					userRepository.save(user);
					System.out.println(">>> [마이그레이션] 유저(" + user.getNickname() + ")의 매너 온도를 기본값 36.5로 보정했습니다.");
				}
			}
		};
	}
}
