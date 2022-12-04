package dermtest_task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.validation.FieldError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.Parameter;

@RequestMapping("/api")
@RestController
public class DoctorController {

  private static final Logger logger = LogManager.getLogger(DoctorController.class);

  @Autowired
  DoctorRepository doctorRepository;

  @Operation(summary = "Get all doctors")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Found doctors", content = {
          @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Doctor.class))) }),
      @ApiResponse(responseCode = "204", description = "No doctors in the database", content = @Content) })
  @GetMapping("/doctors")
  public ResponseEntity<List<Doctor>> getAllDoctors() {
    try {
      List<Doctor> doctors = new ArrayList<Doctor>();

      doctorRepository.findAll().forEach(doctors::add);

      if (doctors.isEmpty()) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }

      return new ResponseEntity<>(doctors, HttpStatus.OK);
    } catch (Exception e) {
      logger.error(String.format("error while fetching doctors: %s", e));
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Operation(summary = "Get single doctor")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Found doctor", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = Doctor.class)) }),
      @ApiResponse(responseCode = "404", description = "Doctor with given id does not exist", content = @Content) })
  @GetMapping("/doctors/{id}")
  public ResponseEntity<Doctor> getDoctorById(@PathVariable("id") Long id) {
    try {
      Optional<Doctor> doctorData = doctorRepository.findById(id);

      if (doctorData.isPresent()) {
        return new ResponseEntity<>(doctorData.get(), HttpStatus.OK);
      } else {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      logger.error(String.format("error while fetching doctor %s: %s", id, e));
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Operation(summary = "Create a new doctor")
  @ApiResponse(responseCode = "201", description = "Added doctor", content = {
      @Content(mediaType = "application/json", schema = @Schema(implementation = Doctor.class)) })
  @PostMapping("/doctors")
  public ResponseEntity<Doctor> addDoctor(
      @Parameter(description = "id field can be omitted from request body") @Valid @RequestBody Doctor doctor) {
    try {
      Doctor savedDoctor = doctorRepository
          .save(new Doctor(doctor.getName(), doctor.getSurname(), doctor.getEmployer(), doctor.getSpeciality()));
      logger.info(String.format("registered a new doctor: %s", savedDoctor));
      return new ResponseEntity<>(savedDoctor, HttpStatus.CREATED);
    } catch (Exception e) {
      logger.error(String.format("error while creating a new doctor: %s", e));
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Operation(summary = "Update a doctor")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated a doctor", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = Doctor.class)) }),
      @ApiResponse(responseCode = "404", description = "Doctor with provided id not found", content = @Content),
  })
  @PutMapping("/doctors/{id}")
  public ResponseEntity<Doctor> updateTutorial(@PathVariable("id") long id, @Valid @RequestBody Doctor doctor) {
    try {
      Optional<Doctor> doctorData = doctorRepository.findById(id);

      if (doctorData.isPresent()) {
        Doctor updatedDoctor = doctorData.get();
        updatedDoctor.setName(doctor.getName());
        updatedDoctor.setSurname(doctor.getSurname());
        updatedDoctor.setEmployer(doctor.getEmployer());
        updatedDoctor.setSpeciality(doctor.getSpeciality());
        return new ResponseEntity<>(doctorRepository.save(updatedDoctor), HttpStatus.OK);
      } else {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      logger.error(String.format("server error while updating a doctor: %s", e));
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ApiResponse(responseCode = "400", description = "Invalid fields in request", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"name\": \"name is mandatory\",\"employer\": \"employer is mandatory\"}")))
  public Map<String, String> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });
    return errors;
  }

  @Operation(summary = "Delete a doctor")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Deleted a doctor"),
      @ApiResponse(responseCode = "404", description = "Doctor with provided id not found", content = @Content),
  })
  @DeleteMapping("/doctors/{id}")
  public ResponseEntity<String> deleteTutorial(@PathVariable("id") long id) {
    try {
      Optional<Doctor> doctorData = doctorRepository.findById(id);
      if (doctorData.isPresent()) {
        doctorRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
      } else {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
    } catch (Exception e) {
      logger.error(String.format("server error while deleting a doctor: %s", e));
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

}
