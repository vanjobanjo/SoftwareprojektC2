package de.fhwedel.klausps.controller;


import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.runner.RunWith;


@RunWith(JUnitPlatform.class)
@SelectPackages({"de.fhwedel.klausps.controller.api",
    "de.fhwedel.klausps.controller.services"
    // add packages here
})
public class TestSuite {
  // don't write anything here
}
