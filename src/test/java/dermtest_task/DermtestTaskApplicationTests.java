package dermtest_task;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(DoctorController.class)
class TestDoctorController {

	@MockBean
	private DoctorRepository doctorRepository;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void returns204_WhenNoDoctorsInDatabase() throws Exception {
		mockMvc.perform(get("/api/doctors"))
				.andExpect(status().isNoContent());
	}

	@Test
	void returnsListOfDoctors() throws Exception {
		var doctors = List.of(new Doctor("Test", "Testerson", "TUK", "GP"),
				new Doctor("Mock", "Mockerson", "PERH", "dermatologist"));

		doReturn(doctors).when(doctorRepository).findAll();

		mockMvc.perform(get("/api/doctors"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].name", is(doctors.get(0).getName())))
				.andExpect(jsonPath("$.[0].surname", is(doctors.get(0).getSurname())))
				.andExpect(jsonPath("$.[1].employer", is(doctors.get(1).getEmployer())))
				.andExpect(jsonPath("$.[1].speciality", is(doctors.get(1).getSpeciality())));
	}

	@Test
	void returnsSingleDoctor() throws Exception {
		long id = 1;
		var doctor = new Doctor("Test", "Testerson", "TUK", "GP");
		doctor.setId(Long.valueOf(1));
		doReturn(Optional.of(doctor)).when(doctorRepository).findById(eq(doctor.getId()));

		mockMvc.perform(get("/api/doctors/{id}", id))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.name", is(doctor.getName())))
				.andExpect(jsonPath("$.surname", is(doctor.getSurname())))
				.andExpect(jsonPath("$.employer", is(doctor.getEmployer())))
				.andExpect(jsonPath("$.speciality", is(doctor.getSpeciality())));
	}

	@Test
	void returns404_WhenDoctorNotFound() throws Exception {
		long id = 1;
		when(doctorRepository.findById(id)).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/doctors/{id}", id))
				.andExpect(status().isNotFound());
	}

	@Test
	void addsSingleDoctor() throws Exception {
		long id = 1;
		Doctor doctor = new Doctor("Test", "Testerson", "TUK", "GP");
		doctor.setId(id);

		when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

		mockMvc.perform(
				post("/api/doctors").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content(
						objectMapper.writeValueAsString(doctor)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value(doctor.getName()))
				.andExpect(jsonPath("$.surname").value(doctor.getSurname()))
				.andExpect(jsonPath("$.employer").value(doctor.getEmployer()))
				.andExpect(jsonPath("$.id").value(Long.toString(id)))
				.andExpect(jsonPath("$.speciality").value(doctor.getSpeciality()));
	}

	@Test
	void returns400_WhenInvalidInput() throws Exception {
		long id = 1;
		Doctor doctor = new Doctor("", "  ", null, "		");

		mockMvc.perform(
				post("/api/doctors").accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON).content(
						objectMapper.writeValueAsString(doctor)))
				.andExpect(status().isBadRequest());

		mockMvc.perform(
				put("/api/doctors/{id}", id).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
						.content(
								objectMapper.writeValueAsString(doctor)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void returns404_WhenUpdatingNonExistantDoctor() throws Exception {
		Doctor doctor = new Doctor("Test", "Testerson", "TUK", "GP");
		long id = 1L;
		doctor.setId(id);
		when(doctorRepository.findById(id)).thenReturn(Optional.empty());

		mockMvc.perform(
				put("/api/doctors/{id}", id).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(doctor)))
				.andExpect(status().isNotFound());
	}

	@Test
	void updatesDoctor() throws Exception {
		long id = 1L;
		Doctor doctor = new Doctor("Test", "Testerson", "TUK", "GP");
		doctor.setId(id);

		Doctor updatedDoctor = new Doctor("Update", "Updaton", "TUK", "surgeon");

		when(doctorRepository.findById(id)).thenReturn(Optional.of(doctor));
		when(doctorRepository.save(any(Doctor.class))).thenReturn(updatedDoctor);

		mockMvc.perform(
				put("/api/doctors/{id}", id).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(doctor)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value(updatedDoctor.getName()))
				.andExpect(jsonPath("$.surname").value(updatedDoctor.getSurname()))
				.andExpect(jsonPath("$.employer").value(updatedDoctor.getEmployer()))
				.andExpect(jsonPath("$.speciality").value(updatedDoctor.getSpeciality()));
	}

	@Test
	void deletesSingleDoctor() throws Exception {
		long id = 1L;
		var doctor = new Doctor("Test", "Testerson", "TUK", "GP");
		doctor.setId(Long.valueOf(id));
		doReturn(Optional.of(doctor)).when(doctorRepository).findById(eq(doctor.getId()));

		mockMvc.perform(delete("/api/doctors/{id}", id))
				.andExpect(status().isOk());
	}

	@Test
	void returns404_WhenNoDoctorToDelete() throws Exception {
		long id = 1L;
		when(doctorRepository.findById(id)).thenReturn(Optional.empty());

		mockMvc.perform(delete("/api/doctors/{id}", id))
				.andExpect(status().isNotFound());
	}

}
