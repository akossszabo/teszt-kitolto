package xyz.codingmentor.ejb;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.interceptor.Interceptors;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import xyz.codingmentor.ejb.facade.EntityFacade;
import xyz.codingmentor.entity.Course;
import xyz.codingmentor.entity.FilledTest;
import xyz.codingmentor.entity.Student;
import xyz.codingmentor.entity.Test;
import xyz.codingmentor.interceptor.LoggerInterceptor;

@ManagedBean
@SessionScoped
@Interceptors({LoggerInterceptor.class})
public class StudentHomepageController implements Serializable {

    @EJB
    private EntityFacade entityFacade;
    private final ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
    private Student activeStudent;
    private String selectedCourse;
    private List<String> selectableCourses;
    private List<Test> selectableTests;
    private List<Test> searchedTests;
    private List<FilledTest> selectableFilledTests;
    private StreamedContent profilePicture;

    public StudentHomepageController() {
        selectableCourses = new ArrayList<>();
        selectableCourses.add("ALL");
        selectableTests = new ArrayList<>();
        searchedTests = new ArrayList<>();
        selectableFilledTests = new ArrayList();
    }

    public void load() {
        activeStudent = entityFacade.namedQueryOneParam("STUDENT.getByEmail", Student.class, "email", ec.getRemoteUser()).get(0);
        profilePicture = getImage();
    }

    public Student getActiveStudent() {
        return activeStudent;
    }

    public void setActiveStudent(Student activeStudent) {
        this.activeStudent = activeStudent;
    }

    private StreamedContent getImage() {
        if (activeStudent.getImage() == null) {
            return null;
        }
        return new DefaultStreamedContent(new ByteArrayInputStream(activeStudent.getImage()));
//        if (FacesContext.getCurrentInstance().getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
//            return new DefaultStreamedContent();
//        } else {
//            return new DefaultStreamedContent(new ByteArrayInputStream(activeStudent.getImage()));
//        }
    }

    public StreamedContent getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(StreamedContent profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getSelectedCourse() {
        return selectedCourse;
    }

    public void setSelectedCourse(String selectedCourse) {
        this.selectedCourse = selectedCourse;
    }

    public List<String> getSelectableCourses() {
        addCourseNamesToSelectOneMeniList();
        return selectableCourses;
    }

    public void setSelectableCourses(List<String> selectableCourses) {
        this.selectableCourses = selectableCourses;
    }

    public void setSelectableTests(List<Test> selectableTests) {
        this.selectableTests = selectableTests;
    }

    public void setSelectableFilledTests(List<FilledTest> selectableFilledTests) {
        this.selectableFilledTests = selectableFilledTests;
    }

    public List<Test> getSelectableTests() {
        selectableTests.clear();
        if (selectableCourses != null && selectableCourses.size() > 0) {
            setTestsList();
        }

        return selectableTests;
    }

    public List<FilledTest> getSelectableFilledTests() {
        selectableFilledTests.clear();
        if (selectableCourses != null && selectableCourses.size() > 0) {
            setFilledTestsList();
        }

        return selectableFilledTests;
    }

    private void setTestsList() {
        if (selectedCourse == null || selectedCourse.equals(selectableCourses.get(0))) {
            for (Course course : activeStudent.getCourses()) {
                findTestsToCourses(course);
            }
        } else {
            Course course = entityFacade.namedQueryOneParam("COURSE.findByName", Course.class, "name", selectedCourse).get(0);
            findTestsToCourses(course);
        }
    }

    private void setFilledTestsList() {
        if (selectedCourse == null || selectedCourse.equals(selectableCourses.get(0))) {
            for (Course course : activeStudent.getCourses()) {
                findFilledTestsToCourses(course);
            }
        } else {
            Course course = entityFacade.namedQueryOneParam("COURSE.findByName", Course.class, "name", selectedCourse).get(0);
            findFilledTestsToCourses(course);
        }
    }

    private void findTestsToCourses(Course course) {
        searchedTests = entityFacade.namedQueryOneParam("TEST.findByCourseId", Test.class, "course", course);
        for (Test test : searchedTests) {
            findNotFilledTests(test);
        }
    }

    private void findNotFilledTests(Test test) {
        List<FilledTest> filledTests = entityFacade.namedQueryTwoParam("FILLEDTEST.findByStudentIdAndTestIdAndReady", FilledTest.class, "studentId", activeStudent.getId(), "testId", test.getId());
        if (filledTests.isEmpty()) {
            selectableTests.add(test);
        }
    }

    private void findFilledTestsToCourses(Course course) {
        searchedTests = entityFacade.namedQueryOneParam("TEST.findByCourseId", Test.class, "course", course);
        for (Test test : searchedTests) {
            findFilledTests(test);
        }
    }

    private void findFilledTests(Test test) {
        List<FilledTest> filledTests = entityFacade.namedQueryTwoParam("FILLEDTEST.findByStudentIdAndTestIdAndReady", FilledTest.class, "studentId", activeStudent.getId(), "testId", test.getId());
        selectableFilledTests.addAll(filledTests);
    }

    public boolean isSelectedTestStartable(Test selectedTest) {
        if (selectedTest != null) {
            return selectedTest.getQuestions().size() > 0
                    && entityFacade.namedQueryTwoParam(
                            "FILLEDTEST.findByStudentIdAndTestId", FilledTest.class,
                            "studentId", activeStudent.getId(), "testId", selectedTest.getId()).isEmpty();
        } else {
            return false;
        }
    }

    public boolean isSelectedTestContinuable(Test selectedTest) {
        if (selectedTest != null) {
            return selectedTest.getQuestions().size() > 0
                    && entityFacade.namedQueryTwoParam(
                            "FILLEDTEST.findByStudentIdAndTestId", FilledTest.class,
                            "studentId", activeStudent.getId(), "testId", selectedTest.getId()).size() > 0;
        } else {
            return false;
        }
    }

    public boolean isThereAnyAvailableCourse() {
        addCourseNamesToSelectOneMeniList();
        return selectableCourses.size() > 1;
    }

    private void addCourseNamesToSelectOneMeniList() {
        for (Course c : activeStudent.getCourses()) {
            if (!selectableCourses.contains(c.getName())) {
                selectableCourses.add(c.getName());
            }
        }
    }
}
