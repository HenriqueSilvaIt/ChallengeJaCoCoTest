package com.devsuperior.dsmovie.services;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
public class MovieServiceTests {
	
	@InjectMocks
	private MovieService service;

	@Mock
	private MovieRepository  repository;

	private MovieEntity movieEntity;

	private MovieDTO movieDTO;

	private Long existingMovieId;
    private Long nonExistingMovieId;
	private Long dependentMovieId;

	private PageImpl<MovieEntity> page;

	@BeforeEach
	void setUp() throws Exception{

		movieEntity = MovieFactory.createMovieEntity();
		movieDTO = MovieFactory.createMovieDTO();

		page = new PageImpl<>(List.of(movieEntity));

		existingMovieId = 1L;
		nonExistingMovieId = 2L;
		dependentMovieId = 5L;


		// findAll repository

		Mockito.when(repository.searchByTitle(any(), (Pageable) any())).thenReturn(page);

		// FindById

		Mockito.when(repository.findById(existingMovieId)).thenReturn(Optional.of(movieEntity));
		/*sempre no mock é a entidade que retorna porque o repository retorna  a entidade*/

		Mockito.when(repository.findById(nonExistingMovieId)).thenThrow(ResourceNotFoundException.class);

		//Insert
		Mockito.when(repository.save(any())).thenReturn(movieEntity);

		// Update
		Mockito.when(repository.getReferenceById(existingMovieId)).thenReturn(movieEntity);
		Mockito.when(repository.getReferenceById(nonExistingMovieId)).thenThrow(EntityNotFoundException.class);

		//Delete
		/*testeando o if se o id existe ou não*/

		Mockito.when(repository.existsById(existingMovieId)).thenReturn(true);
		Mockito.when(repository.existsById(nonExistingMovieId)).thenReturn(false);
		Mockito.when(repository.existsById(dependentMovieId)).thenReturn(true);

		Mockito.doNothing().when(repository).deleteById(existingMovieId);
		Mockito.doThrow(ResourceNotFoundException.class).when(repository).deleteById(nonExistingMovieId);
		Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentMovieId);

	}

	@Test
	public void findAllShouldReturnPagedMovieDTO() {

		//Arrange
		Pageable pageable = PageRequest.of(1, 10);

		//Act
		Page<MovieDTO> result =  service.findAll("Test Movie", pageable);

		//Assertion
		 Assertions.assertNotNull(result);
		 Assertions.assertEquals(result.get().iterator().next().getTitle(), "Test Movie");

	}
	
	@Test
	public void findByIdShouldReturnMovieDTOWhenIdExists() {

		//Arrange and Act
		MovieDTO result = service.findById(existingMovieId);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getId(), existingMovieId);

	}
	
	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {

			MovieDTO result = service.findById(nonExistingMovieId);
		});
	}
	
	@Test
	public void insertShouldReturnMovieDTO() {



		MovieDTO result = service.insert(movieDTO);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getTitle(), movieEntity.getTitle());
	}
	
	@Test
	public void updateShouldReturnMovieDTOWhenIdExists() {

		MovieDTO result = service.update(existingMovieId, movieDTO);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getId(), existingMovieId);

	}
	
	@Test
	public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {

			MovieDTO result = service.update(nonExistingMovieId, movieDTO);
		});
	}
	
	@Test
	public void deleteShouldDoNothingWhenIdExists() {


		Assertions.assertDoesNotThrow(() -> {

			/*Act*/
			service.delete(existingMovieId);

		});

	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {

			service.delete(nonExistingMovieId);

		});
	}
	
	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependentId() {

		Assertions.assertThrows(DatabaseException.class, () -> {

			service.delete(dependentMovieId);
		});

	}
}
