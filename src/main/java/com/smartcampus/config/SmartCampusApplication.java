package com.smartcampus.config;

import com.smartcampus.filter.ApiLoggingFilter;
import com.smartcampus.resouce.DiscoveryResource;
import com.smartcampus.mapper.GlobalExceptionMapper;
import com.smartcampus.mapper.LinkedResourceNotFoundExceptionMapper;
import com.smartcampus.mapper.RoomNotEmptyExceptionMapper;
import com.smartcampus.resouce.SensorResource;
import com.smartcampus.resouce.SensorRoomResource;
import com.smartcampus.mapper.SensorUnavailableExceptionMapper;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        classes.add(DiscoveryResource.class);
        classes.add(SensorRoomResource.class);
        classes.add(SensorResource.class);

        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        classes.add(SensorUnavailableExceptionMapper.class);
        classes.add(GlobalExceptionMapper.class);

        classes.add(ApiLoggingFilter.class);

        return classes;
    }
}
