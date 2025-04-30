package com.zjx.star.springai.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zjx.star.springai.entity.po.Course;
import com.zjx.star.springai.entity.po.CourseReservation;
import com.zjx.star.springai.entity.po.School;
import com.zjx.star.springai.entity.query.CourseQuery;
import com.zjx.star.springai.mapper.CourseMapper;
import com.zjx.star.springai.mapper.CourseReservationMapper;
import com.zjx.star.springai.mapper.SchoolMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 *2025/4/30:18:44
 *version:1.0.0
 *@author:zjx
 */
@Component
public class CourseTools {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private CourseReservationMapper courseReservationMapper;

    @Autowired
    private SchoolMapper schoolMapper;

    @Tool(description = "根据条件查询课程")
    public List<Course> queryCourse(@ToolParam(description = "查询的条件") CourseQuery course) {

        if (course == null) {
            return List.of();
        }
        LambdaQueryWrapper<Course> courseLambdaQueryWrapper = new LambdaQueryWrapper<>();
        courseLambdaQueryWrapper.eq(course.getType()!=null,Course::getType, course.getType());
        courseLambdaQueryWrapper.le(course.getEdu()!=null,Course::getEdu, course.getEdu());
        if (course.getSorts() != null && !course.getSorts().isEmpty()) {
            for (CourseQuery.Sort sort : course.getSorts()) {
                courseLambdaQueryWrapper.orderBy(true,sort.getAsc(),a->sort.getField());
            }
        }

        return courseMapper.selectList(courseLambdaQueryWrapper);
    }

    @Tool(description = "查询所有校区")
    public List<School> querySchool() {

        return schoolMapper.selectList(null);

    }

    @Tool(description = "生成课程预约单,并返回生成的预约单号")
    public String generateCourseReservation(@ToolParam(description = "预约课程") String courseName,
                                            @ToolParam(description = "学生姓名")String studentName,
                                            @ToolParam(description = "联系电话")String contactInfo,
                                            @ToolParam(description = "预约校区")String school,
                                            @ToolParam(description = "备注",required = false) String remark) {
        CourseReservation courseReservation = new CourseReservation();
        courseReservation.setCourse(courseName);
        courseReservation.setStudentName(studentName);
        courseReservation.setContactInfo(contactInfo);
        courseReservation.setSchool(school);
        courseReservation.setRemark(remark);
        courseReservationMapper.insert(courseReservation);
        return String.valueOf(courseReservation.getId());
    }

}
