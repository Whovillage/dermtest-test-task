package dermtest_task;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;

@Entity
@Table(name="doctors")
@Data
@NoArgsConstructor
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Name is mandatory")
    private String name;
    @NotBlank(message = "Surname is mandatory")
    private String surname;
    @NotBlank(message = "Employer is mandatory")
    private String employer;
    @NotBlank(message = "Speciality is mandatory")
    private String speciality;

    public Doctor(String name, String surname, String employer, String speciality) {
        this.name = name;
        this.surname = surname;
        this.employer = employer;
        this.speciality = speciality;
    }
}
