package biz.netcentric.cq.tools.actool.helper;



import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;

import org.apache.sling.api.SlingHttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.netcentric.cq.tools.actool.authorizableutils.AuthorizableConfigBean;
import biz.netcentric.cq.tools.actool.authorizableutils.AuthorizableDumpUtils;
import biz.netcentric.cq.tools.actool.configuration.CqActionsMapping;


public class AclDumpUtils {

	private static final Logger LOG = LoggerFactory.getLogger(AclDumpUtils.class);
	public final static int PRINCIPAL_BASED_SORTING = 1;
	public final static int PATH_BASED_SORTING = 2;
	public final static int DENY_ALLOW_ACL_SORTING= 1;
	public final static int NO_ACL_SORTING = 2;


/**
 * returns a dump of the ACEs installed in the system using a PrintWriter.
 * @param out PrintWriter
 * @param aceMap map containing all ACE data, either path based or group based
 * @param mapOrdering 
 * @param aceOrdering
 */
	public static void returnAceDump(final PrintWriter out, Map<String, Set<AceBean>> aceMap, final int mapOrdering, final int aceOrdering){

		if(mapOrdering == PATH_BASED_SORTING){
			LOG.debug("path based ordering required therefor getting path based ACE map");
			int aclOrder = NO_ACL_SORTING;
			
			if(aceOrdering == DENY_ALLOW_ACL_SORTING){
				aclOrder = DENY_ALLOW_ACL_SORTING;
			}
			aceMap = AcHelper.getPathBasedAceMap(aceMap, aclOrder) ;
		}

		Set<String> keySet = aceMap.keySet();
		for(String principal:keySet){
			Set<AceBean> aceBeanSet = aceMap.get(principal);
			out.println("- " + principal + ":");
			for(AceBean bean : aceBeanSet){
				out.println();
				out.println("   - path: " + bean.getJcrPath());
				out.println("     permission: " + bean.getPermission());
				out.println("     actions: " + bean.getActionsString());
				out.println("     privileges: " + bean.getPrivilegesString());
				out.print("     repGlob: ");
				if(!bean.getRepGlob().isEmpty()){
					out.println("'" + bean.getRepGlob() + "'");
				}else{
					out.println();
				}
			}
			out.println();
		}
		out.println();
	}

	public static void returnAceDumpAsFile(final SlingHttpServletResponse response, final Map<String, Set<AceBean>> aceMap, final int mapOrder) throws IOException{
		String mimetype =  "application/octet-stream";
		response.setContentType(mimetype);
		ServletOutputStream outStream = null;
		try{
			try {
				outStream = response.getOutputStream();
			} catch (IOException e) {
				LOG.error("Exception in AclDumpUtils: {}", e);
			}

			String fileName = "ACE_Dump_" + new Date(System.currentTimeMillis());
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");



			try {

				writeAclConfigToStream(aceMap, mapOrder, outStream);
			} catch (IOException e) {
				LOG.error("Exception in AclDumpUtils: {}", e);
			}
		}finally{
			if(outStream != null){
				outStream.close();
			}
		}
	}
	
	public static String returnConfigurationDumpAsString(final Map<String, Set<AceBean>> aceMap, final Set<AuthorizableConfigBean> authorizableSet, final int mapOrder) throws IOException{
		
		StringBuilder sb = new StringBuilder(20000);
		AuthorizableDumpUtils.getAuthorizableConfigAsString(sb, authorizableSet);
		returnAceDumpAsString(sb, aceMap, mapOrder);
		
		return sb.toString();
	}
	
	public static StringBuilder returnAceDumpAsString(final StringBuilder sb, final Map<String, Set<AceBean>> aceMap, final int mapOrder) throws IOException{
		
		Set<String> keys = aceMap.keySet();
		sb.append("- " + Constants.ACE_CONFIGURATION_KEY + ":") ;
		sb.append("<br /><br />");
		
		for(String mapKey : keys){

			Set<AceBean> aceBeanSet = aceMap.get(mapKey);
			
			sb.append(Constants.DUMP_INDENTATION_KEY + "- " + mapKey + ":");
			sb.append("<br />");
			for(AceBean bean : aceBeanSet){
				bean = CqActionsMapping.getAlignedPermissionBean(bean);

				sb.append("<br />");
				if(mapOrder == PATH_BASED_SORTING){
					sb.append(Constants.DUMP_INDENTATION_FIRST_PROPERTY + "- principal: " + bean.getPrincipalName()).append("<br />");
				}else if(mapOrder == PRINCIPAL_BASED_SORTING){
					sb.append(Constants.DUMP_INDENTATION_FIRST_PROPERTY + "- path: " + bean.getJcrPath()).append("<br />");
				}
				sb.append(Constants.DUMP_INDENTATION_PROPERTY + "permission: " + bean.getPermission()).append("<br />");
				sb.append(Constants.DUMP_INDENTATION_PROPERTY + "actions: " + bean.getActionsString()).append("<br />");
				sb.append(Constants.DUMP_INDENTATION_PROPERTY + "privileges: " + bean.getPrivilegesString()).append("<br />");
				sb.append(Constants.DUMP_INDENTATION_PROPERTY + "repGlob: ").append("<br />");
				if(!bean.getRepGlob().isEmpty()){
					sb.append("'" + bean.getRepGlob() + "'");
				}else{
					sb.append("<br />");
				}

			}
			sb.append("\n");
		}
		sb.append("\n");
		
		
		
		return sb;
	}

	public static void returnConfigurationDumpAsFile(final SlingHttpServletResponse response,
			Map<String, Set<AceBean>> aceMap, Set<AuthorizableConfigBean> authorizableSet, final int mapOrder) throws IOException{
		
		String mimetype =  "application/octet-stream";
		response.setContentType(mimetype);
		ServletOutputStream outStream = null;
		try{
			try {
				outStream = response.getOutputStream();
			} catch (IOException e) {
				LOG.error("Exception in AclDumpUtils: {}", e);
			}

			String fileName = "ACL_Configuration_Dump_" + new Date(System.currentTimeMillis());
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

			try {
				AuthorizableDumpUtils.writeAuthorizableConfigToStream(authorizableSet, outStream);
				outStream.println() ;
				writeAclConfigToStream(aceMap, mapOrder, outStream);
			} catch (IOException e) {
				LOG.error("Exception in AclDumpUtils: {}", e);
			}
		}finally{
			if(outStream != null){
				outStream.close();
			}
		}
	}
	
    
	private static ServletOutputStream writeAclConfigToStream(
			Map<String, Set<AceBean>> aceMap, final int mapOrder,
			ServletOutputStream outStream) throws IOException {
		Set<String> keys = aceMap.keySet();
		outStream.println("- " + Constants.ACE_CONFIGURATION_KEY + ":") ;
		outStream.println() ;

		for(String mapKey : keys){

			Set<AceBean> aceBeanSet = aceMap.get(mapKey);

			outStream.println(Constants.DUMP_INDENTATION_KEY + "- " + mapKey + ":");

			for(AceBean bean : aceBeanSet){
            bean = CqActionsMapping.getAlignedPermissionBean(bean);
  
				outStream.println();
				if(mapOrder == PATH_BASED_SORTING){
					outStream.println(Constants.DUMP_INDENTATION_FIRST_PROPERTY + "- principal: " + bean.getPrincipalName());
				}else if(mapOrder == PRINCIPAL_BASED_SORTING){
					outStream.println(Constants.DUMP_INDENTATION_FIRST_PROPERTY + "- path: " + bean.getJcrPath());
				}
				outStream.println(Constants.DUMP_INDENTATION_PROPERTY + "permission: " + bean.getPermission());
				outStream.println(Constants.DUMP_INDENTATION_PROPERTY + "actions: " + bean.getActionsString());
				outStream.println(Constants.DUMP_INDENTATION_PROPERTY + "privileges: " + bean.getPrivilegesString());
				outStream.print(Constants.DUMP_INDENTATION_PROPERTY + "repGlob: ");
				if(!bean.getRepGlob().isEmpty()){
					outStream.println("'" + bean.getRepGlob() + "'");
				}else{
					outStream.println();
				}

			}

			outStream.println();
		}
		outStream.println();
		return outStream;
	}

	public static String getDumplLinks(){
		StringBuilder sb = new StringBuilder(); 
		sb.append("path based dump <a href = '" + Constants.ACE_SERVLET_PATH + "?dumpAll=true&keyOrder=pathBased&aceOrder=denyallow'> (download)</a>");
		sb.append("<br />");
		sb.append("group based dump <a href = '" + Constants.ACE_SERVLET_PATH + "?dumpAll=true&aceOrder=denyallow'> (download)</a>");

		return sb.toString();
	}
}
