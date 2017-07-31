package com.zcon.azure.filter;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Vyankatesh
 *
 */
public class CORSFilter implements Filter {
	final String AUTH_HEADER_NAME = "X-AUTH-TOKEN";
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		System.out.println("Filtering on...........................................................");
		HttpServletResponse response = (HttpServletResponse) res;
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE,HEAD");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers", "x-requested-with, Content-Type,Authorization,x-ms-date,x-ms-version");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Headers",
				"Origin, X-Requested-With, Content-Type, Accept, Authorization,x-ms-date,x-ms-version" + AUTH_HEADER_NAME);
		response.setHeader("Access-Control-Expose-Headers", "content-length, " + AUTH_HEADER_NAME);
		chain.doFilter(req, res);
	}
	public void init(FilterConfig filterConfig) {
	}
	public void destroy() {
	}
}
