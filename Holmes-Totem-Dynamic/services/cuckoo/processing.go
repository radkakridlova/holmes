package main

import (
	"encoding/json"
	"fmt"
	"strconv"

	"cuckoo/cuckoo"
)

// processReportInfo extracts all the data from the info
// section of the cuckoo report struct.
func processReportInfo(i *cuckoo.TasksReportInfo) []*CrtResult {
	if i == nil {
		return []*CrtResult{}
	}

	// i.machine can be string or struct so we
	// have to determin where to get our info
	machineString := ""
	err := json.Unmarshal(i.Machine, &machineString)
	if err != nil || machineString == "" {
		machineString = "FAILED"
	}

	mStruct := &cuckoo.TasksReportInfoMachine{}
	err = json.Unmarshal(i.Machine, mStruct)
	if err == nil && mStruct != nil {
		machineString = mStruct.Name
	}

	resMap := make(map[string]interface{})
	resMap["started"] = i.Started
	resMap["ended"] = i.Ended
	resMap["analysis_id"] = strconv.Itoa(i.Id)

	return []*CrtResult{&CrtResult{"info", machineString, resMap}}
}

// processReportSignatures extracts all the data from the signatures
// section of the cuckoo report struct.
func processReportSignatures(sigs []*cuckoo.TasksReportSignature) []*CrtResult {
	if sigs == nil {
		return []*CrtResult{}
	}

	l := len(sigs)
	res := make([]*CrtResult, l, l)
	resMap := make(map[string]interface{})

	for k, sig := range sigs {
		resMap["severity"] = strconv.Itoa(sig.Severity)
		resMap["name"] = sig.Name

		res[k] = &CrtResult{
			"signature",
			sig.Description,
			resMap,
		}
	}

	return res
}

// processReportBehavior extracts all the data from the behavior
// section of the cuckoo report struct.
func processReportBehavior(behavior *cuckoo.TasksReportBehavior) []*CrtResult {
	if behavior == nil {
		return []*CrtResult{}
	}

	var res []*CrtResult
	resMap := make(map[string]interface{})

	if behavior.Processes != nil {
		for _, p := range behavior.Processes {
			resMap["process_id"] = strconv.Itoa(p.Id)
			resMap["parent_id"] = strconv.Itoa(p.ParentId)
			resMap["first_seen"] = p.FirstSeen

			res = append(res, &CrtResult{
				"process",
				p.Name,
				resMap,
			})
		}
	}

	// push api calls
	// not mixed in with upper loop so we can make it optional later
	if behavior.Processes != nil {
		pushCounter := 0

		for _, p := range behavior.Processes {

			procDescription := fmt.Sprintf("%s (%d)", p.Name, p.Id)
			for _, c := range p.Calls {

				if pushCounter >= ctx.Config.MaxAPICalls {
					break
				}

				resMap := make(map[string]interface{})
				resMap["category"] = c.Category
				resMap["status"] = c.Status
				resMap["return"] = c.Return
				resMap["timestamp"] = c.Timestamp
				resMap["thread_id"] = c.ThreadId
				resMap["repeated"] = c.Repeated
				resMap["api"] = c.Api
				resMap["id"] = c.Id
				resMap["process"] = procDescription
				resMap["arguments"] = c.Arguments

				res = append(res, &CrtResult{
					"api_call",
					c.Api,
					resMap,
				})
				pushCounter += 1
			}
		}
	}

	if behavior.Summary != nil {
		if behavior.Summary.Files != nil {
			for _, b := range behavior.Summary.Files {
				res = append(res, &CrtResult{
					"file",
					b,
					nil,
				})
			}
		}

		if behavior.Summary.Keys != nil {
			for _, b := range behavior.Summary.Keys {
				res = append(res, &CrtResult{
					"registry_key",
					b,
					nil,
				})
			}
		}

		if behavior.Summary.Mutexes != nil {
			for _, b := range behavior.Summary.Mutexes {
				res = append(res, &CrtResult{
					"mutex",
					b,
					nil,
				})
			}
		}
	}

	return res
}

// Process MAECobjects from report
func processMAECObjects(maecObjects []*cuckoo.TasksMAECObjects) []*CrtResult {
	if maecObjects == nil {
		return []*CrtResult{}
	}

	l := len(maecObjects)
	res := make([]*CrtResult, l, l)
	resMap := make(map[string]interface{})

	for k, obj := range maecObjects {
		resMap["id"] = obj.Id
		resMap["type"] = obj.Type
		resMap["name"] = obj.Name
		resMap["timestamp"] = obj.Timestamp
		resMap["output_object_refs"] = obj.OutputObjectRefs
//		resMap["input_object_refs"] = obj.InputObjectRefs
//		resMap["instance_object_refs"] = obj.InstanceObjectRefs
//		resMap["strings"] = obj.StaticFeatures.Strings

		resMap["static_features"] = obj.StaticFeatures
//		resMap["analysis_metadata"] = obj.AnalysisMetadata
		resMap["triggered_signatures"] = obj.TriggeredSignatures

/*		if obj.OutputObjectRefs != nil {
			for _, b := range obj.OutputObjectRefs {
				res = append(res, &CrtResult{
					"output_object_ref",
					b,
					nil,
				})
			}		
		}

		if obj.InputObjectRefs != nil {
			for _, b := range obj.InputObjectRefs {
				res = append(res, &CrtResult{
					"input_object_ref",
					b,
					nil,
				})
			}		
		}

		if obj.InstanceObjectRefs != nil {
			for _, b := range obj.InstanceObjectRefs {
				res = append(res, &CrtResult{
					"instance_object_ref",
					b,
					nil,
				})
			}		
		}

		if obj.StaticFeatures != nil && obj.String != nil {
			for _, b := range obj.StaticFeatures.Strings {
				res = append(res, &CrtResult{
					"string",
					b,
					nil,
				})
			}		
		}

		if obj.DynamicFeatures != nil && obj.DynamicFeatures.ProcessTree != nil {
			resMap2 := make(map[string]interface{})

			for _, b := range obj.DynamicFeatures.ProcessTree {
				resMap2["ordinal_position"] = b.OrdinalPosition
				resMap2["process_ref"] = b.ProcessRef
				resMap2["initiated_action_refs"] = b.InitiatedActionRefs

		
				res = append(res, &CrtResult{
					"process",
					b.ProcessRef,
					resMap2,
				})
			}		
		}*/

		res[k] = &CrtResult{
			"maec_object",
			obj.Id,
			resMap,
		}
	}	
//TODO
/*
type TasksDynamicFeatures struct {
	ProcessTree []*TasksProcessTree `json:"process_tree"`
}

type TasksProcessTree struct {
	OrdinalPosition     string   `json:"ordinal_position"`
	ProcessRef          string   `json:"process_ref"`
	InitiatedActionRefs []string `json:"initiated_action_refs"`
}

type TasksAnalysisMetadata struct {
	IsAutomated  string   `json:"is_automated"`
	AnalysisType string   `json:"analysis_type"`
	VmRef        string   `json:"vm_ref"`
	ToolRefs     []string `json:"tool_refs"`
	Description  string   `json:"description"`
}

type TasksTriggeredSignatures struct {
	SignatureType string `json:"signature_type"`
	Description   string `json:"description"`
	Severity      string `json:"severity"`
}

type TasksStaticFeatures struct {
	Strings []string `json:"strings"`
}
*/

	return res	
}

/*
// support for dropped files will be added later
func processDropped(m *lib.CheckResultsReq, cuckoo *lib.CuckooConn, crits *lib.CritsConn, upload bool) ([]*CrtResult, error) {
	start := time.Now()

	resp, err := cuckoo.GetDropped(m.TaskId)
	if err != nil {
		return []*CrtResult{}, err
	}

	results := []*CrtResult{}

	respReader := bytes.NewReader(resp)
	unbzip2 := bzip2.NewReader(respReader)
	untar := tar.NewReader(unbzip2)

	for {
		hdr, err := untar.Next()
		if err == io.EOF {
			// end of tar archive
			break
		}

		if err != nil {
			return results, err
		}

		if hdr.Typeflag != tar.TypeReg && hdr.Typeflag != tar.TypeRegA {
			// no real file, might be a dir or symlink
			continue
		}

		name := filepath.Base(hdr.Name)
		fileData, err := ioutil.ReadAll(untar)

		if upload {

			id, err := crits.NewSample(fileData, name)

			// we need to add a short sleep here so tastypie won't crash.
			// this is a very ugly work around but sadly necessary
			time.Sleep(time.Second * 1)

			if err != nil {
				if err.Error() == "empty file" {
					continue
				}

				return results, err
			}

			if err = crits.ForgeRelationship(id); err != nil {
				return results, err
			}

			// see comment above
			time.Sleep(time.Second * 1)
		}

		resMap := make(map[string]interface{})
		resMap["md5"] = fmt.Sprintf("%x", md5.Sum(fileData))

		results = append(results, &CrtResult{
			"file_added",
			name,
			resMap,
		})
	}

	elapsed := time.Since(start)
	c.Debug.Printf("Uploaded %d dropped files in %s [%s]\n", len(results), elapsed, m.CritsData.AnalysisId)

	return results, nil
}
*/
