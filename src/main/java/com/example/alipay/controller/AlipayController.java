package com.example.alipay.controller;

import com.example.alipay.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 支付宝支付信息
 *
 * @author zym
 *
 */
@RequestMapping("/alipay")
@Controller(value = "alipayController")
public class AlipayController extends BaseController{

	private static final Log log = LogFactory.getLog(AlipayController.class);


	/**
	 *支付宝支付
	 *@param:money:商品价格 number：分级个数
	 *@return:
	 *@Author:zym
	 *@Date:2019/2/19
	 */
	@CrossOrigin
	@RequestMapping("/pay.do")
	@ResponseBody
	public void pay(Double money, HttpServletRequest request, HttpServletResponse response) throws Exception {

		// 向支付宝发送请求
		// 客户端的IP地址
		String exter_invoke_ip = IPUtils.getIpFromRequest(request);
		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("out_trade_no", "12345678");
		sParaTemp.put("total_fee", money + "");
		sParaTemp.put("extra_common_param", "11" + ""); // 附加字段，存放userId
		sParaTemp.put("exter_invoke_ip", exter_invoke_ip);

		sParaTemp.put("service", AlipayConfig.service);
		sParaTemp.put("partner", AlipayConfig.partner);
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("payment_type", AlipayConfig.payment_type);
		sParaTemp.put("notify_url", AlipayConfig.notify_url);
		sParaTemp.put("return_url", AlipayConfig.return_url);
		sParaTemp.put("seller_email", AlipayConfig.seller_email);
		sParaTemp.put("seller_id", AlipayConfig.seller_id);
		sParaTemp.put("subject", AlipayConfig.subject);
		sParaTemp.put("body", AlipayConfig.body);
		sParaTemp.put("show_url", AlipayConfig.show_url);
		sParaTemp.put("anti_phishing_key", AlipayConfig.anti_phishing_key);


		// 建立请求
		String sHtmlText = AlipaySubmit.buildRequest(sParaTemp, "POST", "确认");

		writeHtml(request, response, sHtmlText);

		return ;
	}

	/**
	 * 支付宝同步回调接口（同步跳转通知）
	 * 阿里文档：https://docs.open.alipay.com/62/104743/
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 * @throws ServletException
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping("/returnUrl.do")
	public void returnUrl(String trade_status, String out_trade_no, HttpServletRequest request, HttpServletResponse response) throws ServletException,
			Exception {

		log.info("阿里支付宝2：" + String.format("insideOrderId:%s,trade_status:%s", out_trade_no, trade_status));

		// 获取支付宝GET过来反馈信息
		Map<String, String> params = new HashMap<String, String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}
			params.put(name, valueStr);
		}

		// 计算得出通知验证结果
		if (AlipayNotify.verify(params)) {// 验证成功
			if (trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS")) {
				if (paySuccess(params, request)) {
					response.sendRedirect("http://10.19.167.108:8088/index.html#/pandect");
					return;
				} else {
					log.warn("此支付订单更新失败，out_trade_no=" + out_trade_no + ",参数信息：" + params.toString());
					response.sendRedirect("/cp/#/account/bill");
					return;
				}
			}
		} else { // 验证失败
			log.warn("此支付订单验证不通过，out_trade_no=" + out_trade_no + ",参数信息：" + params.toString());
			response.sendRedirect(WebConstatVar.ERROR_PAGE);
			return;
		}
	}

	/**
	 * 异步回调（异步通知）
     * 阿里文档：https://docs.open.alipay.com/62/104743/
	 * @param out_trade_no
	 * @param trade_no
	 * @param trade_status
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping("/notifyUrl.do")
	@ResponseBody
	public String notifyUrl(String out_trade_no, String trade_no, String trade_status, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		log.info("阿里支付宝3：" + String.format("insideOrderId:%s,trade_status:%s", out_trade_no, trade_status));

		// 获取支付宝POST过来反馈信息
		Map<String, String> params = new HashMap<String, String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}
			params.put(name, valueStr);
		}

		log.info("支付宝回调notifyUrl的信息为：" + params.toString());

		if (AlipayNotify.verify(params)) {// 验证成功

			log.info("阿里支付宝4：" + String.format("insideOrderId:%s,验证结果:成功", out_trade_no));

			if (trade_status.equals("TRADE_FINISHED")) {
				if (paySuccess(params, request)) {
					log.info("阿里支付宝6：" + String.format("insideOrderId:%s,处理结果:成功", out_trade_no));
//					return "success";
					return "http://10.19.167.108:8088/index.html#/pandect";

				} else {
					log.info("阿里支付宝7：" + String.format("insideOrderId:%s,处理结果:失败", out_trade_no));
					return "fail";
				}

				// 注意：
				// 该种交易状态只在两种情况下出现
				// 1、开通了普通即时到账，买家付款成功后。
				// 2、开通了高级即时到账，从该笔交易成功时间算起，过了签约时的可退款时限（如：三个月以内可退款、一年以内可退款等）后。
			} else if (trade_status.equals("TRADE_SUCCESS")) {
				if (paySuccess(params, request)) {
					log.info("阿里支付宝8：" + String.format("insideOrderId:%s,处理结果:成功", out_trade_no));
					return "http://10.19.167.108:8088/index.html#/pandect";
				} else {
					log.info("阿里支付宝9：" + String.format("insideOrderId:%s,处理结果:失败", out_trade_no));
					return "fail";
				}

				// 注意：
				// 该种交易状态只在一种情况下出现——开通了高级即时到账，买家付款成功后。
			}
			return "http://10.19.167.108:8088/index.html#/pandect";
		} else {// 验证失败
			log.info("阿里支付宝5：" + String.format("insideOrderId:%s,验证结果:失败", out_trade_no));
			return "fail";
		}
	}

	/**
	 * 支付成功的处理逻辑<br>
	 * 1、判断该笔订单是否在商户网站中已经做过处理<br>
	 * 2、如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序<br>
	 * 3、如果有做过处理，不执行商户的业务程序
	 *
	 * @param params 支付宝的参数信息
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public boolean paySuccess(Map<String, String> params, HttpServletRequest request) throws ServletException, IOException {
		String trade_status = params.get("trade_status");// 交易状态
		String out_trade_no = params.get("out_trade_no"); // 商户订单号
		String trade_no = params.get("trade_no"); // 支付宝交易号
		String extra_common_param = params.get("extra_common_param"); // 附加字段，存放userId

		// 判断该笔订单是否在商户网站中已经做过处理
		// 如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序

			String total_fee = params.get("total_fee");
			Double money = Double.valueOf(total_fee);

			log.info("阿里支付宝11：" + String.format("insideOrderId:%s,订单金额:%f", out_trade_no,money));

		return true;
	}

}
