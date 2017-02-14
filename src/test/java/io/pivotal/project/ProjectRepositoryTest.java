package io.pivotal.project;

import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.time.LocalDate;

import static org.assertj.core.api.Java6Assertions.assertThat;


public class ProjectRepositoryTest {

    private ProjectRepository projectRepository;
    private Flyway flyway;

    @Before
    public void setup() {
        SingleConnectionDataSource dataSource = new SingleConnectionDataSource("jdbc:mysql://localhost:3306/burndown_test", "root", "", true);
        flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.migrate();

        projectRepository = new ProjectRepository(dataSource);
    }

    @After
    public void teardown() {
        flyway.clean();
    }

    @Test
    public void getProjectEntityById_returnsEntitySavedInDB() {
        ProjectEntity projectEntity = new ProjectEntity("test-name", LocalDate.of(1999, 12, 31), 750);
        projectRepository.save(projectEntity);

        ProjectEntity savedEntity = projectRepository.getProjectEntityById(1);

        assertThat(savedEntity.getId()).isEqualTo(1);
        assertThat(savedEntity.getName()).isEqualTo("test-name");
        assertThat(savedEntity.getStartDate()).isEqualTo(LocalDate.of(1999, 12, 31));
        assertThat(savedEntity.getHourlyRate()).isEqualTo(750);
    }

    @Test
    public void save_returnsFullyHydratedProjectEntity() {
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName("name-to-save");
        projectEntity.setHourlyRate(365);
        projectEntity.setStartDate(LocalDate.of(2018, 8, 30));

        ProjectEntity savedProjectEntity = projectRepository.save(projectEntity);

        assertThat(savedProjectEntity.getId()).isEqualTo(1);
        assertThat(savedProjectEntity.getName()).isEqualTo("name-to-save");
        assertThat(savedProjectEntity.getHourlyRate()).isEqualTo(365);
        assertThat(savedProjectEntity.getStartDate()).isEqualTo(LocalDate.of(2018, 8, 30));
    }
}