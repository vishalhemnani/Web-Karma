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
package edu.isi.karma.modeling.alignment;

import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.semantictypes.SemanticTypePredictionThread;
import edu.isi.karma.modeling.semantictypes.crfmodelhandler.CRFModelHandler;
import edu.isi.karma.rep.*;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.SemanticType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class AlignmentManager {
	private static HashMap<String, Alignment> alignmentMap = null;
	private static AlignmentManager _InternalInstance = null;
	private static HashSet<IAlignmentSaver> alignmentSavers = null;
	public static AlignmentManager Instance()
	{
		if (_InternalInstance == null)
		{
			_InternalInstance = new AlignmentManager();
			alignmentMap = new HashMap<String, Alignment>();
			alignmentSavers = new HashSet<IAlignmentSaver>();
		}
		return _InternalInstance;
	}
	
	public void addAlignmentToMap(String key, Alignment alignment) {
		alignmentMap.put(key, alignment);
	}

	public Alignment getAlignment(String alignmentId) {
		return alignmentMap.get(alignmentId);
	}
	
	public String getAlignmentId(Alignment alignment) {
		for(String id: alignmentMap.keySet()) {
			if(alignmentMap.get(id) == alignment)
				return id;
		}
		return null;
	}
	
	public Alignment getAlignment(String workspaceId, String worksheetId) {
		String alignmentId = constructAlignmentId(workspaceId, worksheetId);
		return getAlignment(alignmentId);
	}

	public void addAlignmentSaver(IAlignmentSaver saver) {
		alignmentSavers.add(saver);
		for(Alignment alignment : alignmentMap.values())
			alignment.addSaver(saver);
	}
	
	public Alignment getAlignmentOrCreateIt(String workspaceId, String worksheetId, OntologyManager ontologyManager){
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(
				workspaceId, worksheetId);
		
		Workspace workspace = WorkspaceManager.getInstance().getWorkspace(workspaceId);
		Worksheet worksheet = WorkspaceManager.getInstance().getWorkspace(workspaceId).getWorksheet(worksheetId);
		CRFModelHandler crfModelHandler = workspace.getCrfModelHandler();
		Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		
		if (alignment == null) {
			alignment = new Alignment(ontologyManager);
			for(IAlignmentSaver saver : alignmentSavers)
				alignment.addSaver(saver);
			AlignmentManager.Instance().addAlignmentToMap(alignmentId, alignment);
		}
	
		List<HNodePath> paths = new ArrayList<>();
		for (HNodePath path : worksheet.getHeaders().getAllPaths()) {
			HNode node = path.getLeaf();
			String hNodeId = node.getId();
			Node n = alignment.getNodeById(hNodeId);
			if (n == null) {
				paths.add(path);
				alignment.addColumnNode(hNodeId, node.getColumnName(), null, new ArrayList<SemanticType>());
			} else if (n instanceof ColumnNode) {
				ColumnNode c =  ((ColumnNode)n);
				if (c.getCrfSuggestedSemanticTypes() == null || c.getCrfSuggestedSemanticTypes().isEmpty())
					paths.add(path);
			}
		}
			
		if (!paths.isEmpty()) {
			Thread t = new Thread(new SemanticTypePredictionThread(worksheet, paths, crfModelHandler, ontologyManager, alignment));
			t.start();
		}
		
		return alignment;
	}
	public void removeWorkspaceAlignments(String workspaceId) {
		ArrayList<String> keysToBeRemoved = new ArrayList<String>();
		for(String key:alignmentMap.keySet()) {
			if(key.startsWith(workspaceId+":")) {
				keysToBeRemoved.add(key);
			}
		}
		// Remove the keys
		for(String key:keysToBeRemoved) {
			alignmentMap.remove(key);
		}
	}
	
	public String constructAlignmentId(String workspaceId, String worksheetId) {
		return workspaceId + ":" + worksheetId + "AL";
	}
	
	public String getWorkspaceId(Alignment align) {
		String id = getAlignmentId(align);
		if(id != null) {
			int idx = id.indexOf(":");
			id = id.substring(0, idx);
		}
		return id;
	}
	
	public String getWorksheetId(Alignment align) {
		String id = getAlignmentId(align);
		if(id != null) {
			int idx = id.indexOf(":");
			int endIdx = id.indexOf("AL", idx);
			id = id.substring(idx+1, endIdx);
		}
		return id;
	}
}
