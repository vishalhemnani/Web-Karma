/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

package edu.isi.karma.kr2rml.mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.kr2rml.planning.TriplesMapGraph;

public class KR2RMLMappingAuxillaryInformation {
	private static Logger LOG = LoggerFactory.getLogger(KR2RMLMappingAuxillaryInformation.class);
	private TriplesMapGraph triplesMapGraph;
	private Map<String, List<String>> blankNodesColumnCoverage;
	private Map<String, String> blankNodesUriPrefixMap;
	private Map<String, List<PredicateObjectMap>> columnNameToPredObjMLinks;
	private Map<String, String> subjectMapIdToTemplateAnchor;
	
	public KR2RMLMappingAuxillaryInformation() {
		this.triplesMapGraph = new TriplesMapGraph();
		this.blankNodesUriPrefixMap = new HashMap<String, String>();
		this.blankNodesColumnCoverage = new HashMap<String, List<String>>();
		this.columnNameToPredObjMLinks = new HashMap<String, List<PredicateObjectMap>>();
		this.subjectMapIdToTemplateAnchor = new HashMap<String, String>();
	}

	public TriplesMapGraph getTriplesMapGraph() {
		return triplesMapGraph;
	}

	public Map<String, List<String>> getBlankNodesColumnCoverage() {
		return blankNodesColumnCoverage;
	}

	public Map<String, String> getBlankNodesUriPrefixMap() {
		return blankNodesUriPrefixMap;
	}
	
	public Map<String, List<PredicateObjectMap>> getColumnNameToPredObjLinks() {
		return this.columnNameToPredObjMLinks;
	}
	
	public Map<String, String> getSubjectMapIdToTemplateAnchor() {
		return subjectMapIdToTemplateAnchor;
	}

	//TODO move this
	public static String findSubjectMapTemplateAnchor(
			List<String> columnsCovered) {
		int maxDepth = Integer.MIN_VALUE;
		String anchor = null;
		for(String column : columnsCovered)
		{
			int depth = 0;
			depth = calculateColumnDepth(column, depth);
			if(depth > maxDepth)
			{
				maxDepth = depth;
				anchor = column;
			}
		}
		LOG.debug("found anchor " + anchor + " at " + maxDepth);
		return anchor;
	}

	private static int calculateColumnDepth(String column, int depth) {
		if(column.charAt(0) == '[')
		{
			for(int i = 0; i < column.length(); i++)
			{
				if(column.charAt(i) == ',')
				{
					depth++;
				}
			}
		}
		return depth;
	}	
}
