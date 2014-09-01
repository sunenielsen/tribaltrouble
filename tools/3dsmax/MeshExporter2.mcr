--macroScript TTexporter category:"Tools" tooltip:"Export to Tribal Trouble" (

	fn WriteHeader out_file = (
		-- print header
		format "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\"?>\n\n" to:out_file
	
		format "<!DOCTYPE mesh [\n" to:out_file
		format "	<!ELEMENT mesh       (polygons)>\n" to:out_file
		format "	<!ELEMENT polygons   (polygon+)>\n" to:out_file
		format "	<!ELEMENT polygon    (vertex+)>\n" to:out_file
		format "	<!ELEMENT vertex     (skin+)>\n\n" to:out_file
		format "	<!ELEMENT skin        EMPTY>\n\n" to:out_file

--		format "	<!ATTLIST mesh texture CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST vertex x CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST vertex y CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST vertex z CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST vertex r CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST vertex g CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST vertex b CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST vertex a CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST vertex nx CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST vertex ny CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST vertex nz CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST vertex u CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST vertex v CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST vertex u2 CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST vertex v2 CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST skin bone CDATA #REQUIRED>\n" to:out_file
		format "	<!ATTLIST skin weight CDATA #REQUIRED>\n" to:out_file
		format "]>\n\n" to:out_file
	)
	


	fn WriteMesh obj physiqued = (
		if physiqued == true do (
			physiqueOps.setFigureMode obj true
		)
                filename = obj.name + ".xml"
		out_file = createfile filename
		WriteHeader out_file
		tmesh = snapshotAsMesh obj
		
		num_verts = tmesh.numverts
		verts = #()
		for i = 1 to num_verts do (
			vert = getVert tmesh i
			append verts vert
		)
		
		num_tverts = meshop.getNumMapVerts tmesh 1
		tverts = #()
		for i = 1 to num_tverts do (
			tvert = meshop.getMapVert tmesh 1 i
			append tverts tvert
		)
		
		num_maps = meshop.getNumMaps tmesh
		map2_support = meshop.getMapSupport tmesh 2
		save_map2 = num_maps >= 2 and map2_support
		
		tverts_map2 = #()
		if save_map2 then (
			num_tverts2 = meshop.getNumMapVerts tmesh 2
			for i = 1 to num_tverts2 do (
				tvert = meshop.getMapVert tmesh 2 i
				append tverts_map2 tvert																								
			)
		)

		
--		texture_name = ""
--		if obj.material != undefined do (
--			if (classof (obj.material.diffusemap) == Mask) then (
--				texture_name = getfilenamefile obj.material.diffusemap.map.filename
--			) else if (classof (obj.material.diffusemap) == BitmapTexture) do (
--				texture_name = getfilenamefile obj.material.diffusemap.filename
--			)
--		)
		format "<mesh>\n" to:out_file
		format "\t<polygons>\n" to:out_file
		num_faces = tmesh.numfaces
		for i = 1 to num_faces do (
			format "\t\t<polygon>\n" to:out_file
			face = getFace tmesh i
			three_normals = (meshop.getFaceRNormals tmesh i )
			for j = 1 to 3 do (
				tvface = meshop.getMapFace tmesh 1 i
				format "\t\t\t<vertex x=\"%\" y=\"%\" z=\"%\"\n" verts[face[j]].x verts[face[j]].y verts[face[j]].z to:out_file
				format "\t\t\t\tr=\"%\" g=\"%\" b=\"%\" a=\"%\"\n" 1 1 1 1 to:out_file
				format "\t\t\t\tnx=\"%\" ny=\"%\" nz=\"%\"\n" three_normals[j].x three_normals[j].y three_normals[j].z to:out_file
				format "\t\t\t\tu=\"%\" v=\"%\"\n" tverts[tvface[j]].x tverts[tvface[j]].y to:out_file
				if save_map2 then (
					tvface_map2 = meshop.getMapFace tmesh 2 i
					format "\t\t\t\tu2=\"%\" v2=\"%\">\n" tverts_map2[tvface_map2[j]].x tverts_map2[tvface_map2[j]].y to:out_file
				) else (
					format "\t\t\t\tu2=\"0.0\" v2=\"0.0\">\n" to:out_file
				)

				if physiqued == true then (
					bones = physiqueOps.getVertexBones obj face[j]
					num_bones = bones.count
					for k = 1 to num_bones do (
						format "\t\t\t\t\t<skin bone=\"%\" weight=\"%\"/>\n" bones[k].name (physiqueOps.getVertexWeight obj face[j] k) to:out_file
					)
				) else (
					format "\t\t\t\t\t<skin bone=\"dummy_bone\" weight=\"1\"/>\n" to:out_file
				)
				format "\t\t\t</vertex>\n" to:out_file
			)
			format "\t\t</polygon>\n" to:out_file
		)
		format "\t</polygons>\n" to:out_file

		format "</mesh>\n" to:out_file
		close out_file
		format "Saved %\n" filename
		if physiqued == true do (
			physiqueOps.setFigureMode obj false
		)
	)

	-------------------------------------------------------------------------------
	-- Main -----------------------------------------------------------------------
	-------------------------------------------------------------------------------

--for obj in objects where classof obj == Editable_Mesh do (
for obj in objects do (
	physiqued = true
	try (
		format "%: %\n" obj.name (physiqueOps.getphysiquemodifier obj)
	) catch (
		format "%: non-physique\n" obj.name
		physiqued = false
	)
	WriteMesh obj physiqued
)
