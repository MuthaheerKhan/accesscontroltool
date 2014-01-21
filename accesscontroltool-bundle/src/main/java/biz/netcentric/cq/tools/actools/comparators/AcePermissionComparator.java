package biz.netcentric.cq.tools.actools.comparators;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;

import biz.netcentric.cq.tools.actool.helper.AceBean;

public class AcePermissionComparator implements Comparator<AceBean>{

	@Override
	public int compare(final AceBean ace1,final AceBean ace2) {
		if(StringUtils.equals(ace1.getPermission(), "allow") && StringUtils.equals(ace2.getPermission(), "deny") ){
			return 1;
		}
		else  if(StringUtils.equals(ace1.getPermission(), "deny") && StringUtils.equals(ace2.getPermission(), "allow")){
			return -1;
		}
		// if default return value was 0 no new entry would get added in case of TreeSet, the result would be a Set containg extactly 2 elements
		// (one deny and one allow), therefore default value here is 1 this ensures a grouping of ACEs in one block containing
		// all denies followed by a block containing all allows
		return 1;
	}

}
