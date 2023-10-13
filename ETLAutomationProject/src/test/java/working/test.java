package working;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class test {

	@Test (dataProvider="getFolderPath")
	public void test1(String path) {
		System.out.println(path);
	}

	@DataProvider
	public Object[] getFolderPath() {

		File file = new File("./testcases");
		String[] subfolders = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});
		Object[] obj = new Object[subfolders.length];
		List<String> folderList=new ArrayList();
		
		for (String s : subfolders) {
			folderList.add(s);
		}
		obj=folderList.toArray(obj);
	
		System.out.println(Arrays.toString(obj));

		return obj;

	}

}
