package djboxbox;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.vpo.djvoxbox.DjvbApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DjvbApplication.class)
@WebAppConfiguration
public class DjvbApplicationTests {

	@Test
	public void contextLoads() {
	}

}
