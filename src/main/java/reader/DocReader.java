package reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;
import model.DocRowModel;

public class DocReader {

	private Map<String, DocRowModel> db = new HashMap<String, DocRowModel>();

	public void readFromCVS(String path) {
		File file = new File(path);
		CSVReader reader = null;
		try {
			reader = new CSVReader(new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8")), ',');

			List<String[]> records;
			records = reader.readAll();

			for (String[] record : records) {
				if ("TYPE".equals(record[0]))
					continue;

				DocRowModel data = new DocRowModel();

				int type = Integer.parseInt(record[0]);
				data.setType(type);
				data.setName(record[1]);
				data.setColumnAttribute(record[2]);
				data.setCname(record[3]);
				data.setDesc(record[4]);
				data.setDefaultValue(record[5]);

				if ("N".equals(record[6])) {
					data.setInParam(false);
				} else {
					data.setInParam(true);
				}

				data.setParamName(record[7]);

				String prefix = "";
				if (type == 1) {
					prefix = "IN-";
				} else if (type == 2) {
					prefix = "OUT-";
				}

				db.put(prefix + data.getName(), data);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
			}
		}
	}

	public void resetDb() {
		db.clear();
	}

	public Map<String, DocRowModel> getDb() {
		return db;
	}

	public void showData() {
		Set<Entry<String, DocRowModel>> iter = db.entrySet();

		for (Entry<String, DocRowModel> item : iter) {
			System.out.println("key:" + item.getKey() + ", value:" + item.getValue());
		}
	}

}
