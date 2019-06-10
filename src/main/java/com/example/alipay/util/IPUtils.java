package com.example.alipay.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

public class IPUtils {
	/**
	 * 获取字符串的反转形式
	 * 
	 * @param sourceIP
	 *            原始IP
	 * @return 反转形式的IP字符串
	 */
	public final static String reserve(String sourceIP) {
		String outputIP;
		StringBuilder sb = new StringBuilder();
		String[] tokens = sourceIP.split("\\.");
		for (int i = tokens.length - 1; i >= 0; i--) {
			sb.append(tokens[i]).append(".");
		}
		outputIP = sb.toString();
		outputIP = outputIP.substring(0, outputIP.length() - 1);
		return outputIP;
	}

	// copy from
	// http://www.codereye.com/2010/01/get-real-ip-from-request-in-java.html
	public static final String _255 = "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
	public static final Pattern pattern = Pattern.compile("^(?:" + _255
			+ "\\.){3}" + _255 + "$");

	public static String longToIpV4(long longIp) {
		int octet3 = (int) ((longIp >> 24) % 256);
		int octet2 = (int) ((longIp >> 16) % 256);
		int octet1 = (int) ((longIp >> 8) % 256);
		int octet0 = (int) ((longIp) % 256);
		return octet3 + "." + octet2 + "." + octet1 + "." + octet0;
	}

	public static long ipV4ToLong(String ip) {
		String[] octets = ip.split("\\.");
		return (Long.parseLong(octets[0]) << 24)
				+ (Integer.parseInt(octets[1]) << 16)
				+ (Integer.parseInt(octets[2]) << 8)
				+ Integer.parseInt(octets[3]);
	}

	/**
	 * 是否为局域网IP. 前提: 参数 ip 需要是一个合法的 ip地址.
	 * 
	 * @param ip
	 * @return
	 */
	public static boolean isIPv4Private(String ip) {
		long longIp = ipV4ToLong(ip);

		return (longIp >= ipV4ToLong("10.0.0.0") && longIp <= ipV4ToLong("10.255.255.255"))
				|| (longIp >= ipV4ToLong("172.16.0.0") && longIp <= ipV4ToLong("172.31.255.255"))
				|| (longIp >= ipV4ToLong("192.168.0.0") && longIp <= ipV4ToLong("192.168.255.255"))
				|| (longIp == ipV4ToLong("127.0.0.1"));
	}

	/**
	 * 是否为有效的IP地址
	 * 
	 * @param ip
	 * @return
	 */
	public static boolean isIPv4Valid(String ip) {
		if (org.apache.commons.lang3.StringUtils.isBlank(ip)) {
			return false;
		}

		return pattern.matcher(ip).matches();
	}

	/**
	 * 根据请求的参数获取IP地址
	 * 
	 * @param request
	 * @return
	 */
	public static String getIpFromRequest(HttpServletRequest request) {
		String ip = request.getHeader("X-Real-IP");
		System.out.println("X-Real-IP=" + ip);

		if (IPUtils.isIPv4Valid(ip) && !IPUtils.isIPv4Private(ip)) {
			return ip;
		}

		// X-Forwarded-For is "ip, ip, ip"
		String ips = request.getHeader("X-Forwarded-For");
		System.out.println("X-Forwarded-For=" + ips);

		System.out.println("remoteAddress=" + request.getRemoteAddr());

		if (StringUtils.isBlank(ips)) {
			return request.getRemoteAddr();
		}

		ips = ips.trim().replaceAll("\\s*", "");
		for (String s : ips.split(",")) {
			if (isIPv4Valid(s) && !isIPv4Private(s)) {
				return s;
			}
		}

		return request.getRemoteAddr();
	}

	public static void main(String[] args) {
		System.out.print(IPUtils.isIPv4Private("172.16.0.15"));
	}
}
