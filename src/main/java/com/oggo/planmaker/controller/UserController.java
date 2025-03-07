package com.oggo.planmaker.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oggo.planmaker.mapper.UserMapper;
import com.oggo.planmaker.model.User;

@RestController
@RequestMapping(value = "/user")
public class UserController {

	@Autowired
	UserMapper mapper;

	@PostMapping(value = "/join")
	public void join(@RequestBody User user) {
		mapper.join(user);
	}

	@PostMapping(value = "/login")
	public ResponseEntity<?> login(@RequestBody User user) {
		try {
			User loginUser = mapper.login(user);

			// 사용자 정보 확인
			if (loginUser != null) {
				return ResponseEntity.ok(loginUser);
			} else {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("이메일이나 비밀번호가 잘못되었습니다.");
			}
		} catch (Exception e) {
			// 예외 처리
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("요청을 처리하는 동안 오류가 발생했습니다");
		}
	}

	@PostMapping(value = "/googleLogin")
	public User googleLogin(@RequestBody User user) {

//		System.out.println(user.toString());

		User isUser = mapper.firstCheck(user);
		if (isUser == null) {
			mapper.googleJoin(user);
		}
		User checkuser = mapper.googleLogin(isUser);

//		System.out.println(checkuser.toString());

		return checkuser;
	}

	@GetMapping(value = "/checkId")
	public HashMap<String, Object> checkId(@RequestParam String userId) {
		System.out.println("아이디체크");
		boolean available = !mapper.existsByUserId(userId);
		HashMap<String, Object> response = new HashMap<>();
		response.put("available", available);
		return response;
	}

	@GetMapping(value = "/checkEmail")
	public HashMap<String, Object> checkEmail(@RequestParam String userEmail) {
		System.out.println("이메일체크");
		boolean available = !mapper.existsByUserEmail(userEmail);
		HashMap<String, Object> response = new HashMap<>();
		response.put("available", available);
		return response;
	}

	@Transactional
	@PostMapping(value = "/editProfile")
	public User editProfile(@RequestBody User user) {


		System.out.println(user.toString());
		
		if(user.getUserPw().equals("")) {
			user.setUserPw(mapper.getUserById(user).getUserPw());
		}
		
	    User returnUser = null;
	    int row = mapper.editProfile(user);
	    if (row > 0) {
	    	returnUser = mapper.getUserById(user);
	    }
	    System.out.println(returnUser.toString());


	    return returnUser;
	}
}
