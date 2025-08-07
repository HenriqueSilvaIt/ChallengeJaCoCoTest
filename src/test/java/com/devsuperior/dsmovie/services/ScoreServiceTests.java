package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.ScoreEntityPK;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.projections.UserDetailsProjection;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import com.devsuperior.dsmovie.tests.ScoreFactory;
import com.devsuperior.dsmovie.tests.UserDetailsFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
public class ScoreServiceTests {
	
	@InjectMocks
	private ScoreService service;

	@Mock
	private UserService userService;

	@Mock
	private MovieRepository  movieRepository;

	@Mock
	private ScoreRepository repository;

	private MovieEntity movieEntity;

	private UserEntity admin, selfScore;

	private Long existingMovieId;
	private Long nonExistingMovieId;

	private ScoreEntity scoreEntity;
	private ScoreDTO scoreDTO;


	@BeforeEach
	void setUp() throws Exception {

		existingMovieId = 1L;
		nonExistingMovieId = 2L;

		admin = UserFactory.createUserEntity();

		movieEntity = MovieFactory.createMovieEntity();
		movieEntity.setId(existingMovieId);


		scoreEntity = ScoreFactory.createScoreEntity();
		scoreEntity.setMovie(movieEntity);


		scoreDTO = ScoreFactory.createScoreDTO();

		Mockito.when(movieRepository.existsById(existingMovieId)).thenReturn(true);
		Mockito.when(movieRepository.existsById(nonExistingMovieId)).thenReturn(false);

		Mockito.when(movieRepository.findById(existingMovieId)).thenReturn(Optional.of(movieEntity));
		Mockito.when(movieRepository.findById(nonExistingMovieId)).thenThrow(ResourceNotFoundException.class);

		Mockito.when(repository.saveAllAndFlush(any())).thenReturn(List.of(scoreEntity));
		Mockito.when(movieRepository.save(any())).thenReturn(movieEntity);

		Mockito.doThrow(ResourceNotFoundException.class).when(movieRepository).findById(nonExistingMovieId);

	}

	
	@Test
	public void saveScoreShouldReturnMovieDTO() {


		Mockito.when(userService.authenticated()).thenReturn(admin);

		movieEntity.setId(existingMovieId);

		MovieDTO result = service.saveScore(scoreDTO);


		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getId(), existingMovieId);

	}
	
	@Test
	public void saveScoreShouldThrowResourceNotFoundExceptionWhenNonExistingMovieId() {

		Mockito.when(userService.authenticated()).thenReturn(admin);

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {

		  movieEntity.setId(nonExistingMovieId);
		  scoreEntity.setMovie(movieEntity);
		  scoreDTO = new ScoreDTO(scoreEntity);

			MovieDTO result = service.saveScore(scoreDTO);
		});


	}
}
