package io.pivotal.project;

import io.pivotal.project.burndown.BurndownCsvParser;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ProjectServiceTest {

    private ProjectRepository mockProjectRepository;
    private BurndownCsvParser mockBurndownCsvParser;
    private ProjectService projectService;

    @Before
    public void setup() {
        mockProjectRepository = mock(ProjectRepository.class);
        mockBurndownCsvParser = mock(BurndownCsvParser.class);

        projectService = new ProjectService(mockProjectRepository, mockBurndownCsvParser);
    }

    @Test
    public void getProjectById_setsProjectFieldsFromRepositoryEntity() {
        LocalDate stubbedLocalDate = LocalDate.of(2017, 2, 13);
        int stubbedHourlyRate = 500;
        ProjectEntity stubbedProjectEntity = new ProjectEntity(123, "foo-bar", stubbedLocalDate, stubbedHourlyRate);
        when(mockProjectRepository.getProjectEntityById(56)).thenReturn(stubbedProjectEntity);

        Optional<Project> project = projectService.getProjectById(56);

        Project actualProject = project.get();
        assertThat(actualProject.getId()).isEqualTo(123);
        assertThat(actualProject.getName()).isEqualTo("foo-bar");
        assertThat(actualProject.getStartDate()).isEqualTo(stubbedLocalDate);
        assertThat(actualProject.getHourlyRate()).isEqualTo(stubbedHourlyRate);
    }

    @Test
    public void getProjectById_setsBurndownListFromCsvParser() {
        ProjectEntity stubbedProjectEntity = new ProjectEntity(2, "some-name-that-matches-openair", null, 0);
        when(mockProjectRepository.getProjectEntityById(100)).thenReturn(stubbedProjectEntity);

        ArrayList<Float> stubbedBurndown = new ArrayList<>();
        when(mockBurndownCsvParser.getBurndownByProjectName("some-name-that-matches-openair")).thenReturn(stubbedBurndown);

        Optional<Project> project = projectService.getProjectById(100);

        Project actualProject = project.get();
        assertThat(actualProject.getBurndown()).isSameAs(stubbedBurndown);
    }

    @Test
    public void getProjectById_defaultsBurndownToAnEmptyList() {
        when(mockProjectRepository.getProjectEntityById(anyInt())).thenReturn(new ProjectEntity());
        when(mockBurndownCsvParser.getBurndownByProjectName(anyString())).thenReturn(null);

        Project project = projectService.getProjectById(new Random().nextInt()).get();

        assertThat(project.getBurndown()).isEmpty();
    }

    @Test
    public void saveProject_delegatesToProjectRepository() {
        when(mockProjectRepository.save(any())).thenReturn(new ProjectEntity());

        projectService.saveProject(new Project("project-name", 150, LocalDate.MAX));

        ArgumentCaptor<ProjectEntity> captor = ArgumentCaptor.forClass(ProjectEntity.class);
        verify(mockProjectRepository).save(captor.capture());
        ProjectEntity value = captor.getValue();
        assertThat(value.getName()).isEqualTo("project-name");
        assertThat(value.getHourlyRate()).isEqualTo(150);
        assertThat(value.getStartDate()).isEqualTo(LocalDate.MAX);
    }

    @Test
    public void saveProject_returnsNewlyCreatedProject() {
        ProjectEntity stubbedProject = new ProjectEntity(10, "baz", LocalDate.MIN, 75);
        when(mockProjectRepository.save(any())).thenReturn(stubbedProject);

        Project createdProject = projectService.saveProject(new Project());

        assertThat(createdProject.getId()).isEqualTo(10);
        assertThat(createdProject.getName()).isEqualTo("baz");
        assertThat(createdProject.getHourlyRate()).isEqualTo(75);
        assertThat(createdProject.getStartDate()).isEqualTo(LocalDate.MIN);
        assertThat(createdProject.getBurndown()).isEmpty();
    }

}