package xyz.codingmentor.entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

/**
 *
 * @author Olivér
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "TEST.listByTeacher",
            query = "SELECT t FROM Test t WHERE t.teacher.id=:teacherId")
})
public class Test implements Serializable {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private Integer duration;
    @ManyToOne
    @JoinColumn(name = "COURSE_ID")
    private Course course;
    @OneToMany(mappedBy = "test")
    private List<Question> questions;
    @ManyToOne
    @JoinColumn(name = "TEACHER_ID")
    private Teacher teacher;
    @OneToMany(mappedBy = "test")
    private List<FilledTest> filledTests;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public List<FilledTest> getFilledTests() {
        return filledTests;
    }

    public void setFilledTests(List<FilledTest> filledTests) {
        this.filledTests = filledTests;
    }
    
    
    
}
