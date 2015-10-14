package ua.com.kerriline.location;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.CharStreams;

public class SourceSheetTest {
	String text = "ABCD     : 1234\nEFGI     : 5678\nEFGI     : 9012";
	
	@Test
	public void testText2Table() throws Exception {
		SourceSheet ss = new SourceSheet();
		
		List<Map<String,String>> expecteds = new ArrayList<Map<String,String>>();
		Map<String,String> map = new HashMap<String, String>();
		map.put("ABCD", "1234");
		map.put("EFGI", "5678");
		expecteds.add(map);
		
		List<Map<String,String>> results = ss.text2table(text);
		
		
		Assert.assertEquals(expecteds, results);
	}
	
	@Test
	public void testName() throws Exception {
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("sample1.txt");
		String sample1 = CharStreams.toString(new InputStreamReader(is));

		SourceSheet ss = new SourceSheet();
		List<Map<String,String>> results = ss.text2table(sample1);
	}
}
