package org.kakooge.dacho.dm.process;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.kakooge.dacho.dm.model.Security;

/**
 * 
 * @author mawandm
 *
 */
public final class DownloadTask implements Callable<Security> {

	private final HttpClient httpClient;
	private final Security security;
	private static final Logger logger = Logger.getLogger(DownloadTask.class.getName());
	private static final boolean debug = System.getProperty("debug")!=null;
	

	public DownloadTask(final HttpClient httpClient, final Security security) {
		this.httpClient = httpClient;
		this.security = security;
	}

	/**
	 * This helps us build a URI for the symbol to send to finance.yahoo.com
	 * 
	 * @param symbol
	 * @return
	 * @throws Exception
	 */
	private URI buildURI(String symbol) throws Exception {
		// http://download.finance.yahoo.com/d/quotes.csv?s=SORL&f=sl1d1t1c1ohgv&e=.csv
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost("download.finance.yahoo.com")
				.setPath("/d/quotes.csv").setParameter("s", symbol)
				.setParameter("f", "sl1d1t1c1ohgv").setParameter("e", ".csv");
		return builder.build();
	}

	/**
	 * Download Yahoo prices
	 * 
	 * @param httpclient
	 *            the httpClient to connect with
	 * @param symbol
	 *            the symbol to query
	 * @return
	 * @throws Exception
	 */
	private String download() throws Exception {
		String symbolString = Security.getSymbolString(security);
		URI uri = buildURI(symbolString);

		if (debug)
			logger.info("Downloading symbol " + symbolString);

		HttpGet httpget = new HttpGet(uri);
		HttpResponse response = httpClient.execute(httpget);
		HttpEntity entity = response.getEntity();
		StringBuilder sb = new StringBuilder();

		if (entity != null) {
			InputStream instream = entity.getContent();
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						(instream)));

				String output;
				// System.out.println("Output from Server .... \n");
				while ((output = br.readLine()) != null) {
					sb.append(output);
				}
			} finally {
				instream.close();
			}
		}

		return sb.length() > 0 ? sb.toString() : null;
	}

	/**
	 * This pattern allows us to clean some of that Yahoo data that is always
	 * quoted
	 */
	private final Pattern pattern = Pattern.compile("\"*");

	private Security clean(String data) throws Exception {

		String[] field = data.split(",");
		Security record = new Security();
		try {

			if (debug)
				logger.info("Cleaning symbol " + field[0]);

			Matcher m = pattern.matcher(field[0]);

			record.symbol = Security.getSymbolLong(m.replaceAll(""));
			record.high = Double.parseDouble(field[6]);

			m = pattern.matcher(field[2]);
			record.date = new SimpleDateFormat("M/dd/yyyy").parse(
					m.replaceAll("")).getTime();

			record.open = Double.parseDouble(field[5]);
			record.close = Double.parseDouble(field[1]);
			record.low = Double.parseDouble(field[7]);
			record.volume = Double.parseDouble(field[8]);
		} catch (NumberFormatException nfe) {
			logger.log(
					Level.WARNING,
					String.format(
							"Could not parse record '%s'; ignoring with exception '%s'",
							data, nfe.getMessage()));
			return null;
		} catch (ParseException pe) {
			logger.log(
					Level.WARNING,
					String.format(
							"Could not parse record '%s'; ignoring with exception '%s'",
							data, pe.getMessage()));
			return null;
		}

		return record;
	}

	@Override
	public Security call() throws Exception {
		String line = download();
		return clean(line);
	}

}
