package com.healthbuzz.healthbuzz;

import java.util.ArrayList;
import java.util.Collections;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

class Processor {
    private final static String TAG = Processor.class.getSimpleName();
    private final int windowSize;
    private final int strideSize;

    Processor(int windowSize, int strideSize) {
        this.windowSize = windowSize;
        this.strideSize = strideSize;
    }

    private ArrayList<Attribute> getAttributes(String[] attributes) {
        ArrayList<Attribute> attInfo = new ArrayList<>();
        for (String attribute : attributes) {
            attInfo.add(new Attribute(attribute));
        }
        return attInfo;
    }

    private Instance mean(Instances window) {
        int numAxis = window.numAttributes();
        Instance mean = new DenseInstance(numAxis);
        for (int i = 0; i < numAxis; i++) {
            mean.setValue(i, window.meanOrMode(i));
        }
        return mean;
    }

    private Instance var(Instances window) {
        int numAxis = window.numAttributes();
        Instance var = new DenseInstance(numAxis);
        for (int i = 0; i < numAxis; i++) {
            var.setValue(i, window.variance(i));
        }
        return var;
    }

    Instances extractFeaturesAndAddLabels(Instances data, int label) {
        Instances meanFeatures = new Instances("mean", getAttributes(new String[]{"mean x", "mean y", "mean z"}), InferenceActivity.initialInstancesSize);
        Instances varFeatures = new Instances("var", getAttributes(new String[]{"var x", "var y", "var z"}), InferenceActivity.initialInstancesSize);
        Instances labels = new Instances("label", new ArrayList<Attribute>(Collections.singletonList(InferenceActivity.labelAttr)), InferenceActivity.initialInstancesSize);
        for (int head = 0; head + this.windowSize <= data.size(); head += this.strideSize) {
            Instances window = new Instances(data, head, this.windowSize);
            meanFeatures.add(mean(window));
            varFeatures.add(var(window));

            Instance labelInstance = new DenseInstance(1);
            labelInstance.setValue(0, label);
            labels.add(labelInstance);
        }
        Instances featureData = Instances.mergeInstances(meanFeatures, varFeatures);
        featureData = Instances.mergeInstances(featureData, labels);
        featureData.setClass(InferenceActivity.labelAttr);
        return featureData;
    }
}
