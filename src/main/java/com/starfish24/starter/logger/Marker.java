package com.starfish24.starter.logger;

import lombok.experimental.UtilityClass;
import org.slf4j.MarkerFactory;

@UtilityClass
public class Marker {
    public static final org.slf4j.Marker BUSINESS = MarkerFactory.getMarker("BUSINESS");
    public static final org.slf4j.Marker BUSINESS_FATAL = MarkerFactory.getMarker("BUSINESS_FATAL");
    public static final org.slf4j.Marker VALIDATION = MarkerFactory.getMarker("VALIDATION");
    public static final org.slf4j.Marker AUTH = MarkerFactory.getMarker("AUTH");
    public static final org.slf4j.Marker DATABASE = MarkerFactory.getMarker("DATABASE");
    public static final org.slf4j.Marker RUNTIME = MarkerFactory.getMarker("RUNTIME");
    public static final org.slf4j.Marker CHECKOUT = MarkerFactory.getMarker("CHECKOUT");
}
