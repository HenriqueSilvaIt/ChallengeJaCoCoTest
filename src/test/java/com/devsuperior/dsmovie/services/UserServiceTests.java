package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.projections.UserDetailsProjection;
import com.devsuperior.dsmovie.repositories.UserRepository;
import com.devsuperior.dsmovie.tests.UserDetailsFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import com.devsuperior.dsmovie.utils.CustomUserUtil;
import org.h2.engine.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class UserServiceTests {

	@InjectMocks
	private UserService service;

	@Mock
	private UserRepository repository;

	private UserEntity user;

	@Mock// tem que mock
	private CustomUserUtil customUserUtil;

	private String existingUserName, nonExistingUserName;

	@Mock
	List<UserDetailsProjection> userDetailsProjections;

	@BeforeEach
	void setUp() throws Exception {

		existingUserName = "alex@gmail.com";
		nonExistingUserName = "maria@gmail.com";

		userDetailsProjections = UserDetailsFactory.createCustomAdminClientUser("alex@gmail.com");

		user = UserFactory.createUserEntity();

		//Retorna usuário atenticado
		Mockito.when(repository.findByUsername(existingUserName)).thenReturn(Optional.of(user));

		//LoadUserByName

		Mockito.when(repository.searchUserAndRolesByUsername(existingUserName)).thenReturn(userDetailsProjections);
		Mockito.when(repository.searchUserAndRolesByUsername(nonExistingUserName)).thenReturn(new ArrayList<>());

}

	@Test
	public void authenticatedShouldReturnUserEntityWhenUserExists() {



		//Arrange
		Mockito.when(customUserUtil.getLoggedUsername()).thenReturn(existingUserName);

		//Act
		UserEntity result = service.authenticated();

		Assertions.assertNotNull(result);
	}

	@Test
	public void authenticatedShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {

		Mockito.doThrow(ClassCastException.class).when(customUserUtil).getLoggedUsername();

		Assertions.assertThrows(UsernameNotFoundException.class, () -> {

			UserEntity result = service.authenticated();

		});
	}

	@Test
	public void loadUserByUsernameShouldReturnUserDetailsWhenUserExists() {

		UserDetails result = service.loadUserByUsername(existingUserName);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getUsername(), existingUserName);


	}

	@Test
	public void loadUserByUsernameShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {

		Assertions.assertThrows(UsernameNotFoundException.class, () -> {

			UserDetails  result = service.loadUserByUsername(nonExistingUserName);
		});

	}
}
