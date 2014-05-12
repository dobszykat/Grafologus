package com.grafologus.main;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Coordinates implements Serializable{
	private static final long serialVersionUID = 1L;
	List<Float> koordtombx = new ArrayList<Float>();
	List<Float> koordtomby = new ArrayList<Float>();
	List<Long> speedarray = new ArrayList<Long>();
	int height = 0;
	int width = 0;
}
