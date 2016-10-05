package com.macklive;

import com.macklive.objects.GameTest;
import com.macklive.objects.TeamTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({GameTest.class,
                     TeamTest.class})
public class JUnitTestSuite {
}
