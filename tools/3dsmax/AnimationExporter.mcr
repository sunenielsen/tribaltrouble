fn WriteHeader out_file = (
	-- print header
	format "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\"?>\n\n" to:out_file

	format "<!DOCTYPE animation [\n" to:out_file
	format "	<!ELEMENT animation  (frame+)>\n" to:out_file
	format "	<!ELEMENT frame      (transform+)>\n" to:out_file
	format "	<!ELEMENT transform   EMPTY>\n" to:out_file

	format "	<!ATTLIST frame index CDATA #REQUIRED>\n" to:out_file
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
	format "]>\n\n" to:out_file
)

fn WriteAnimation obj = (
	filename = GetSaveFileName() 
	if filename != undefined then ( 
		file = createfile filename
		WriteHeader file

		model_bones = physiqueOps.getBones obj
		firstframe = ( animationrange.start as integer ) / ticksperframe 
		lastframe= ( animationrange.end as integer ) / ticksperframe 
		format "start frame % end frame %\n" firstframe lastframe
		format "<animation>\n" to:file
		for i = firstframe to lastframe do at time i (
			format "\t<frame index=\"%\">\n" i to:file
			for bone in model_bones do (
				format "\t\t<transform name=\"%\"\n" bone.name to:file
				-- format it in the GL way: m<column><row>
				format "\t\t\tm00=\"%\" m10=\"%\" m20=\"%\" m30=\"%\"\n" bone.transform.row1.x bone.transform.row2.x bone.transform.row3.x bone.transform.row4.x to:file
				format "\t\t\tm01=\"%\" m11=\"%\" m21=\"%\" m31=\"%\"\n" bone.transform.row1.y bone.transform.row2.y bone.transform.row3.y bone.transform.row4.y to:file
				format "\t\t\tm02=\"%\" m12=\"%\" m22=\"%\" m32=\"%\"\n" bone.transform.row1.z bone.transform.row2.z bone.transform.row3.z bone.transform.row4.z to:file
				format "\t\t\tm03=\"%\" m13=\"%\" m23=\"%\" m33=\"%\"" 0 0 0 1 to:file
				format "/>\n" to:file
			)
			format "\t</frame>\n" to:file
		)
		format "</animation>\n" to:file

		close file
	)
)

for obj in objects where classof obj == Editable_Mesh do (
	physiqued = true
	try (
		format "%: %\n" obj.name (physiqueOps.getphysiquemodifier obj)
	) catch (
		format "%: non-physique\n" obj.name
		physiqued = false
	)
	if physiqued == true do (
		WriteAnimation obj
	)
)

