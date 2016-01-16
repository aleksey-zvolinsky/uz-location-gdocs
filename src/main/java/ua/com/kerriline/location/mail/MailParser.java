package ua.com.kerriline.location.mail;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Splitter;
import com.google.common.base.Splitter.MapSplitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Aleksey
 *
 */
public class MailParser {
	
	private static final Logger LOG = LoggerFactory.getLogger(MailParser.class);
	
	private static final String UPDATE_FIELD = "ОБНОВЛЕНО";
	private static final String SEPARATOR = "ДАННЫЕ";
	
	private static final String[] TO_DELETE = {"-- ИЗ ПОД ----------  ", "== РЕМОНТЫ     ==   : ", "== БЛОК РЕЙСА =====   ", "== ОПИСАНИЕ РЕЙСА ==  "};
	
	private List<String> duplicates = Arrays.asList("СТАНЦИЯ ОПЕРАЦИИ", "ДАТА ОПЕРАЦИИ", "СТАНЦИЯ НАЗНАЧЕНИЯ", "ГРУЗОПОЛУЧАТЕЛЬ", "ГРУЗ", "ГРУЗООТПРАВИТЕЛЬ",
			"ОПРЕРАЦИЯ", "ДОРОГА ПРИЕМА ГРУЗА", "СТАНЦИЯ", "ВИД РЕМОНТА", "ДОРОГА", "ВЧД", "ГРУЗОПОДЪЕМНОСТЬ");
	
	private List<String> idNameFields = Arrays.asList("КОД ГРУЗА", "СТАНЦИЯ НАЗНАЧЕНИЯ-0", "СТАНЦИЯ ОПЕРАЦИИ-0", "СТАНЦИЯ ПРИЕМА ГРУЗА");


	private static final MapSplitter MAP_SPLITTER = Splitter.on('\n')
			.trimResults()
			.omitEmptyStrings()
			.withKeyValueSeparator(Splitter.on(":").trimResults());
	private static final Splitter SPLITTER = Splitter.on("++++++++++++").omitEmptyStrings();
	private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";
	private DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
	
	public List<Map<String, String>> text2table(final MessageBean messageBean) {
		LOG.debug(messageBean.getBody());
		
		List<Map<String, String>> res = new ArrayList<Map<String, String>>();
		int processedTanks = 0;
		try {
			LOG.info("Preparing mail text");
			String text = prepareText(messageBean.getBody());
			LOG.info("Start processing tanks");
			for(String part: SPLITTER.split(text)){
				LOG.debug("Processing " + ++processedTanks + " tank");
				String filtered = preFilter(part);
				LOG.debug(filtered);
				
				Map<String, String> splitted = MAP_SPLITTER.split(filtered);
				Map<String, String> filteredMap = postFilter(splitted);
				filteredMap.put(UPDATE_FIELD, formatter.format(messageBean.getReceivedDate()));
				res.add(filteredMap); 
			}
			LOG.info("Processed {} tanks", processedTanks);
		} catch (IllegalArgumentException e) {
			
			if(e.getMessage().contains("Duplicate key")) {
				duplicates.add(e.getMessage().substring(e.getMessage().indexOf("[")+1, e.getMessage().indexOf("]")));
				LOG.debug(e.getMessage());
				text2table(messageBean);
			} else {
				throw e;
			}
		} catch (Exception e) {
			LOG.error("Failed on {} tank", processedTanks, e);
			throw e;
		}

		return res;
	}

	private String prepareText(String body) {
		String res = body.replace(SEPARATOR, "++++++++++++" + SEPARATOR);
		for(String what: TO_DELETE) {
			res = res.replace(what, "");
		}
		
		StringBuilder b = new StringBuilder();
		int newPos = 0;
		int lastPos = 0;

		while((newPos = res.indexOf("ПОСЛЕДНИИ ОПЕРАЦИИ", lastPos)) != -1) {
			b.append(res.substring(lastPos, newPos));
			lastPos = res.indexOf("КОД ГРУЗА", newPos)-2;
		}
		b.append(res.substring(lastPos));
				
		return b.toString();
	}
	
	private Map<String, String> postFilter(Map<String, String> map) {
		Map<String, String> newMap = new HashMap<>(); 
		newMap.putAll(map);
		splitFieldsWithId(newMap);
		return newMap;
	}
	
	private void splitFieldsWithId(Map<String, String> map) {
		for(String toSplitField: idNameFields) {
			String fullValue = map.get(toSplitField);
			int pos;
			if(fullValue == null || (pos = fullValue.indexOf(" ")) == -1) {
				continue;
			}
			String id = fullValue.substring(0, pos);
			String name = fullValue.substring(pos+1);
			
			map.put(toSplitField + "-ID", id);
			map.put(toSplitField + "-NAME", name);
		}
	}

	private String preFilter(final String block) {
		String s = block;
		s = processDuplicates(s);
		s = processTankNumbers(s);
		return s;
	}
	
	/**
	 * ДАННЫЕ О ВАГОНЕ : 58999913 (СОБСТВЕННЫЙ) ->  ДАННЫЕ О ВАГОНЕ : 58999913
	 * @param block
	 * @return
	 */
	private String processTankNumbers(final String block) {
		String tank = block.substring(0,block.indexOf("\n"));
		String res = tank.substring(0, block.indexOf("(")) + block.substring(block.indexOf("\n")) ;
		return res;
	}

	/**
	 * Add numbers to duplicate columns 
	 * 
	 * @param block
	 * @return
	 */
	private String processDuplicates(final String block) {
		StringBuilder sb = new StringBuilder(block);
		String res = block;

		for(String duplicate: duplicates) {
			sb.setLength(0);
			int i = 0;
			for(String line : Splitter.on("\n").split(res)) {
				List<String> val = Splitter.on(":").trimResults().splitToList(line);
				if(duplicate.equals(val.get(0))) {
					sb.append(duplicate).append("-").append(i++).append(":").append(val.get(1));	
				} else {
					sb.append(line);
				}
				sb.append("\n");
			}
			res = sb.toString();
		}
		return res;
	}

	public List<Map<String, String>> merge(List<Map<String, String>> tanks, List<Map<String, String>> rawData) {
		String tankKey = "вагон";
		String dataKey = "ДАННЫЕ О ВАГОНЕ";
		List<Map<String, String>> result = new ArrayList<>(); 
		for (Map<String, String> tank : tanks) {
			String value = tank.get(tankKey);
			for(Map<String, String> rec : rawData) {
				if(value.equals(rec.get(dataKey))) {
					Map<String, String> merged = new HashMap<>();
					merged.putAll(tank);
					merged.putAll(rec);
					result.add(merged);
					break;
				}
			}
		}
		return result;
	}
	
	public List<String> getObsoleteTanks(List<Map<String, String>> tanks, List<Map<String, String>> oldTanks, String existTankKey) {
		String tankKey = "вагон";
		List<String> result = new ArrayList<>(); 
		List<Map<String, String>> existTanks = new ArrayList<>(); 
		
		for(Map<String, String> rec : existTanks) {
			result.add(rec.get(existTankKey));
		}
		
		existTanks.addAll(oldTanks);
		for (Map<String, String> tank : tanks) {
			String value = tank.get(tankKey);
			result.remove(value);
		}
		return result;
	}
}

