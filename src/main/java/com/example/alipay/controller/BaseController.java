package com.example.alipay.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class BaseController {

	protected static final Log log = LogFactory.getLog(BaseController.class);

	/** 用户名匹配的正则表达式 */
	protected static final String USERNAME_PATTERN = "^[A-Za-z0-9_]{4,20}$";

	/** 手机号码的正则表达式 */
//	protected static final String PHONE_PATTERN = "^[1-9][0-9]{10}$";
	protected static final String PHONE_PATTERN = "^((13[0-9])|(15[0-9])|(18[0-9])|(14[0-9])|(17[0135678])|(19[0-9])|(16[0-9]))\\d{8}$";

	/** 密码匹配的正则表达式 */
	protected static final String PASSWORD_PATTERN = "^[A-Za-z0-9_!@#$%&*]{6,16}$";

	/** 邮箱匹配的正则表达式 */
	protected static final String EMAIL_PATTERN = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";




	/**
	 * 初始化统计的开始时间和结束时间
	 * 
	 * @param timeType
	 * @param strBeginDate
	 * @param strEndDate
	 * @return beginSendDate:开始时间；endSendDate:结束时间
	 */
	public Map<String, Date> initSendDate(Integer timeType, String strBeginDate, String strEndDate) {
		if (timeType == null) {
			timeType = 7;
		}

		// 获取起止时间
		Date beginSendDate = null, endSendDate = null;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (timeType == 0) {// 自定义时间
			try {
				beginSendDate = df.parse(strBeginDate + " 00:00:00");
				endSendDate = df.parse(strEndDate + " 23:59:59");
				if (beginSendDate.after(endSendDate)) {
					timeType = 7;
				}
			} catch (Exception e) {
				timeType = 7;
			}
		}
		if (null == beginSendDate || null == endSendDate) {
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			Calendar calendar = new GregorianCalendar();
			if (timeType == 2) {
				Calendar c = new GregorianCalendar();
				c.add(Calendar.DATE, (0 - timeType) + 1);
				c.set(Calendar.HOUR_OF_DAY, 23);
				c.set(Calendar.MINUTE, 59);
				c.set(Calendar.SECOND, 59);
				endSendDate = c.getTime();
			} else {
				endSendDate = calendar.getTime();
			}
			strEndDate = df2.format(endSendDate).toString();
			calendar.add(Calendar.DATE, (0 - timeType) + 1);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			beginSendDate = calendar.getTime();
			strBeginDate = df2.format(beginSendDate).toString();
		}

		Map<String, Date> dateMap = new HashMap<String, Date>();
		dateMap.put("beginSendDate", beginSendDate);
		dateMap.put("endSendDate", endSendDate);
		return dateMap;

	}

	/**
	 * 导出数据
	 * 
	 * @param fileName 文件名
	 * @param data 数据
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	public void download(String fileName, StringBuilder data, HttpServletRequest request, HttpServletResponse response) throws IOException {
		OutputStream ouputStream = response.getOutputStream();
		try {
			// response.setContentType("application/vnd.ms-excel;charset=gb18030");
			response.setContentType("application/csv;charset=GBK");
			request.setCharacterEncoding("GBK");
			response.setHeader("Content-disposition", "attachment;filename=" + fileName);

			ouputStream.write(data.toString().getBytes("GBK"));
			ouputStream.flush();
			ouputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void download(String fileName, byte[] data, HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			OutputStream out = response.getOutputStream();
			response.setContentType("application/octet-stream");
			response.setHeader("Content-disposition", "attachment;filename=" + fileName);
			out.write(data);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 设置不要缓存数据
	 * 
	 * @param response
	 */
	public void setNoCache(HttpServletResponse response) {
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
	}

	/** 写入信息 */
	private void write(String contentType, HttpServletRequest req, HttpServletResponse resp, String msg) throws ServletException, IOException {
		resp.setContentType(contentType);
		OutputStreamWriter out = new OutputStreamWriter(resp.getOutputStream(), "UTF-8");
		if (msg == null) {
			msg = "";
		}
		out.write(msg);
		resp.setContentLength(msg.getBytes("UTF-8").length);
		out.flush();
	}

	protected void writePlain(HttpServletRequest req, HttpServletResponse resp, String msg) throws ServletException, IOException {
		write("text/plain; charset=UTF-8", req, resp, msg);
	}

	protected void writeHtml(HttpServletRequest req, HttpServletResponse resp, String msg) throws ServletException, IOException {
		write("text/html; charset=UTF-8", req, resp, msg);
	}

	protected void writeJavaScript(HttpServletRequest req, HttpServletResponse resp, String msg) throws ServletException, IOException {
		write("application/x-javascript; charset=\"UTF-8\"", req, resp, msg);
	}

	protected void writeJson(HttpServletRequest req, HttpServletResponse resp, String msg) throws ServletException, IOException {
		write("application/json; charset=\"UTF-8\"", req, resp, msg);
	}

	protected void writeXml(HttpServletRequest req, HttpServletResponse resp, String msg) throws ServletException, IOException {
		write("application/xml; charset=\"UTF-8\"", req, resp, msg);
	}

	protected void writeFile(HttpServletRequest req, HttpServletResponse resp, String fileName, InputStream in) throws ServletException, IOException {
		resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		// resp.addHeader("Content-Length", "" + data.length);
		// resp.setContentType("application/octet-stream; charset=\"UTF-8\"");
		resp.setContentType("application/x-zip-compressed; charset=\"UTF-8\"");
		writeImage(req, resp, in);
	}

	protected void writeExcel(HttpServletRequest req, HttpServletResponse resp, String fileName, OutputStream out) throws ServletException, IOException {
		resp.setHeader("Content-Disposition", "attachment; filename=stat_data.xls");
		resp.setHeader("Content-Type", "application/vnd.ms-excel");
		resp.setContentType("application/vnd.ms-excel; charset=\"UTF-8\"");
		out.flush();
		out.close();
	}

	protected void writeImage(HttpServletRequest req, HttpServletResponse resp, InputStream imageIn) throws ServletException, IOException {
		OutputStream output = resp.getOutputStream();
		BufferedInputStream bis = new BufferedInputStream(imageIn);// 输入缓冲流
		BufferedOutputStream bos = new BufferedOutputStream(output);// 输出缓冲流
		byte data[] = new byte[4096];// 缓冲字节数
		int size = 0;
		size = bis.read(data);
		while (size != -1) {
			bos.write(data, 0, size);
			size = bis.read(data);
		}
		bis.close();
		bos.flush();// 清空输出缓冲流
		bos.close();

		output.close();
	}

}
