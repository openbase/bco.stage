/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.visualization;

/**
 *
 * @author thoren
 */
public class JointPair {
    private final Joints joint1;
    private final Joints joint2;

    public JointPair(Joints joint1, Joints joint2) {
        this.joint1 = joint1;
        this.joint2 = joint2;
    }

    public Joints getJoint1() {
        return joint1;
    }

    public Joints getJoint2() {
        return joint2;
    }
}
