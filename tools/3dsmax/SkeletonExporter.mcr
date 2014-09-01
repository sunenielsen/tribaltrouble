--macroScript TTexporter category:"Tools" tooltip:"Export to Tribal Trouble" (

	fn WriteHeader out_file = (
		-- print header
		format "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\"?>\n\n" to:out_file
	
		format "<!DOCTYPE skeleton [\n" to:out_file
		format "	<!ELEMENT skeleton   (bones, init_pose)>\n\n" to:out_file
		format "	<!ELEMENT bones      (bone+)>\n\n" to:out_file
		format "	<!ELEMENT init_pose  (transform+)>\n\n" to:out_file
		format "	<!ELEMENT bone        EMPTY>\n\n" to:out_file
		format "	<!ELEMENT transform   EMPTY>\n" to:out_file

		format "	<!ATTLIST transform name CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST transform m00 CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST transform m01 CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST transform m02 CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST transform m03 CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST transform m10 CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST transform m11 CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST transform m12 CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST transform m13 CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST transform m20 CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST transform m21 CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST transform m22 CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST transform m23 CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST transform m30 CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST transform m31 CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST transform m32 CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST transform m33 CDATA #REQUIRED>\n" to:out_file

		format "	<!ATTLIST bone name CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST bone parent CDATA #REQUIRED>\n" to:out_file
		format "]>\n\n" to:out_file
	)
	


fn WriteSkeleton obj = (
	filename = GetSaveFileName() 
	if filename != undefined then (
		physiqueOps.setFigureMode obj true
		out_file = createfile filename
		WriteHeader out_file


		format "<skeleton>\n" to:out_file
		format "\t<bones>\n" to:out_file
		model_bones = physiqueOps.getBones obj
		for bone in model_bones do (
			format "\t\t<bone name=\"%\" parent=\"%\"/>\n" bone.name bone.parent.name to:out_file
		)
		format "\t</bones>\n" to:out_file
		format "\t<init_pose>\n" to:out_file
		for bone in model_bones do (
			format "\t\t<transform name=\"%\"\n" bone.name to:out_file
			-- format it in the GL way: m<column><row>
			format "\t\t\tm00=\"%\" m10=\"%\" m20=\"%\" m30=\"%\"\n" bone.transform.row1.x bone.transform.row2.x bone.transform.row3.x bone.transform.row4.x to:out_file
			format "\t\t\tm01=\"%\" m11=\"%\" m21=\"%\" m31=\"%\"\n" bone.transform.row1.y bone.transform.row2.y bone.transform.row3.y bone.transform.row4.y to:out_file
			format "\t\t\tm02=\"%\" m12=\"%\" m22=\"%\" m32=\"%\"\n" bone.transform.row1.z bone.transform.row2.z bone.transform.row3.z bone.transform.row4.z to:out_file
			format "\t\t\tm03=\"%\" m13=\"%\" m23=\"%\" m33=\"%\"" 0 0 0 1 to:out_file
			format "/>\n" to:out_file
		)
		format "\t</init_pose>\n" to:out_file
		format "</skeleton>\n" to:out_file
		close out_file
		format "Saved %\n" filename
		physiqueOps.setFigureMode obj false
	)
)

	-------------------------------------------------------------------------------
	-- Main -----------------------------------------------------------------------
	-------------------------------------------------------------------------------

for obj in objects where classof obj == Editable_Mesh do (
	try (
		format "%: %\n" obj.name (physiqueOps.getphysiquemodifier obj)
		WriteSkeleton obj
	) catch (
		format "%: has no skeleton\n" obj.name
	)
)
