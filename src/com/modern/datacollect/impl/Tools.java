package com.modern.datacollect.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.security.MessageDigest;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Data;

@SuppressWarnings("deprecation")
public class Tools {

	// 获得内容
	public static Elements getBody(String tag, String html) {
		Document jsoup = Jsoup.parse(html);
		Elements elements = jsoup.select(tag);
		return elements;
	}

	// 普通get请求
	public static String getRequest(String url) {
		RecursiveCount rc = new RecursiveCount();
		return getRequest(url, rc.i, null);
	}

	// 普通get请求
	public static String getRequest1(String url) {
		RecursiveCount rc = new RecursiveCount();
		return getRequest1(url, rc.i, null);
	}

	// 普通get请求
	public static String getRequest(String url, String charset) {
		RecursiveCount rc = new RecursiveCount();
		return getRequest(url, rc.i, charset);
	}

	private static String getRequest1(String url, int i, String charset) {
		if (i >= 5) {
			return null;
		}
		sleep();
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse httpResponse = client.execute(httpGet);
			HttpEntity entity = httpResponse.getEntity();
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				if (entity != null) {
					String html = null;
					if (charset == null) {
						html = EntityUtils.toString(entity);
					} else {
						html = EntityUtils.toString(entity, charset);
					}
					// Tools.setCookieStore(httpResponse);
					return html;
				}
			}
			return getRequest1(url, ++i, charset);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return getRequest1(url, ++i, charset);
		} catch (IOException e) {
			e.printStackTrace();
			return getRequest1(url, ++i, charset);
		} finally {
			try {
				client.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			client = null;
		}
	}

	static CookieStore cookieStore = null;
	static HttpClientContext context = null;

	public static void setCookieStore(HttpResponse httpResponse) {
		cookieStore = new BasicCookieStore();
		String setCookie = httpResponse.getFirstHeader("Set-Cookie").getValue();
		String JSESSIONID = setCookie.substring("JSESSIONID=".length(), setCookie.indexOf(";"));
		System.out.println("JSESSIONID:" + JSESSIONID);
		BasicClientCookie cookie = new BasicClientCookie("JSESSIONID", JSESSIONID);
		cookie.setVersion(0);
		cookieStore.addCookie(cookie);
		Tools.setContext();
	}

	public static void setContext() {
		context = HttpClientContext.create();
		Registry<CookieSpecProvider> registry = RegistryBuilder.<CookieSpecProvider> create().register(CookieSpecs.BEST_MATCH, new BestMatchSpecFactory()).register(CookieSpecs.BROWSER_COMPATIBILITY, new BrowserCompatSpecFactory()).build();
		context.setCookieSpecRegistry(registry);
		context.setCookieStore(cookieStore);
	}

	private static String getRequest(String url, int i, String charset) {
		if (i >= 5) {
			return null;
		}
		sleep();
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(url);
		method.setRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		method.setRequestHeader("Connection", "keep-alive");
		method.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.73 Safari/537.36");
		method.setRequestHeader("Host", method.getHostConfiguration().getHost());
		try {
			client.executeMethod(method);
			client.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
			client.getHttpConnectionManager().getParams().setSoTimeout(30000);
			if (method.getStatusCode() == 200) {
				if (charset == null) {
					return method.getResponseBodyAsString();
				} else {
					return new String(method.getResponseBody(), charset);
				}
			}
			return getRequest(url, ++i, charset);
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			method.releaseConnection();
			method = null;
			client = null;
			return getRequest(url, ++i, charset);
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
			method.releaseConnection();
			method = null;
			client = null;
			return getRequest(url, ++i, charset);
		} catch (IOException e) {
			e.printStackTrace();
			method.releaseConnection();
			method = null;
			client = null;
			return getRequest(url, ++i, charset);
		} finally {
			method.releaseConnection();
			method = null;
			client = null;
		}
	}

	// 获得线上文件
	public static String getLineFile(String url, String localDir) {
		if ("".equals(url)) {
			return "";
		}
		RecursiveCount rc = new RecursiveCount();
		return getLineFile(url, localDir, rc.i);
	}

	// 获得线上文件
	private static String getLineFile(String url, String localDir, int i) {
		if (i >= 5) {
			return null;
		}
		sleep();
		HttpClient client = new HttpClient();
		GetMethod get = new GetMethod(url);
		try {
			client.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
			client.getHttpConnectionManager().getParams().setSoTimeout(30000);
			client.executeMethod(get);
			String fn = Tools.getFileName(url);
			File storeFile = new File(localDir, fn);
			FileOutputStream output = new FileOutputStream(storeFile);
			output.write(get.getResponseBody());
			output.close();
			return storeFile.getPath();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			get.releaseConnection();
			get = null;
			return getLineFile(url, localDir, ++i);
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
			get.releaseConnection();
			get = null;
			return getLineFile(url, localDir, ++i);
		} catch (HttpException e) {
			e.printStackTrace();
			get.releaseConnection();
			get = null;
			return getLineFile(url, localDir, ++i);
		} catch (IOException e) {
			e.printStackTrace();
			get.releaseConnection();
			get = null;
			return getLineFile(url, localDir, ++i);
		} finally {
			get.releaseConnection();
			get = null;
		}
	}

	// 复制文件
	public static String copyFile(String source, String dest) {
		File tmpFile = new File(source);
		String fn = getFileName(source);
		File destFile = new File(dest, fn);
		try {
			if (destFile.exists()) {
				return destFile.getPath();
			}
			Files.copy(tmpFile.toPath(), destFile.toPath());
			return destFile.getPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// 复制文件
	@SuppressWarnings("resource")
	public static File copyFileChannel(String source, String dest) {
		if ("".equals(source)) {
			return null;
		}
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		try {
			String fn = getFileName(source);
			File file = new File(dest, fn);
			inputChannel = new FileInputStream(source).getChannel();
			outputChannel = new FileOutputStream(file).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
			return file;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (inputChannel != null) {
					inputChannel.close();
				}
				if (outputChannel != null) {
					outputChannel.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	// 获取文件名
	public static String getFileName(String url) {
		if (url.indexOf("/") != -1) {
			url = url.substring(url.lastIndexOf("/") + 1, url.length());
		} else if (url.indexOf("\\") != -1) {
			url = url.substring(url.lastIndexOf("\\") + 1, url.length());
		}
		if (url.indexOf("?") != -1) {
			url = url.substring(0, url.indexOf("?"));
		}
		return url;
	}

	// 将网站url生成标识
	public static String string2MD5(String inStr) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
			return "";
		}
		char[] charArray = inStr.toCharArray();
		byte[] byteArray = new byte[charArray.length];

		for (int i = 0; i < charArray.length; i++)
			byteArray[i] = (byte) charArray[i];
		byte[] md5Bytes = md5.digest(byteArray);
		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++) {
			int val = ((int) md5Bytes[i]) & 0xff;
			if (val < 16)
				hexValue.append("0");
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();
	}

	// 创建目录
	public static void mkDir(File file) {
		if (file.getParentFile().exists()) {
			file.mkdir();
		} else {
			mkDir(file.getParentFile());
			file.mkdir();
		}
	}

	public static void sleep() {
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static String dataToString(Data data) {
		StringBuffer sb = new StringBuffer();
		sb.append(data.getTitle()).append(data.getContent()).append(data.getAddress()).append(data.getKeywords());
		return sb.toString();
	}
}

class RecursiveCount {
	int i = 0;
}
