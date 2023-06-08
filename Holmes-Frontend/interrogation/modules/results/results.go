package results

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"strconv"
	"strings"
	"time"
	"compress/gzip"
	"bytes"
//	"path/filepath"
//	"io"
	//"os"

	"holmes/Holmes-Frontend/interrogation/context"

	"github.com/gocql/gocql"
)

/*
id timeuuid,
        sha256 text,
        schema_version text,
        user_id text,
        source_id set<text>,
        source_tag set<text>,
        service_name text,
        service_version text,
        service_config text,
        object_category set<text>,
        object_type text,
        results blob,
        tags set<text>,
        execution_time timestamp,
        watchguard_status text,
        watchguard_log list<text>,
        watchguard_version text,
        comment text,
 */
type Result struct {
	Id                string    `json:"id"`
	SHA256            string    `json:"sha256"`
	SchemaVersion     string    `json:"schema_version"`
	UserId            string    `json:"user_id"`
	SourceId          []string  `json:"source_id"`
	SourceTag         []string  `json:"source_tag"`
	ServiceName       string    `json:"service_name"`
	ServiceVersion    string    `json:"service_version"`
	ServiceConfig     string    `json:"service_config"`
	ObjectCategory    []string  `json:"object_category"`
	ObjectType        string    `json:"object_type"`
	Results           []byte    `json:"results"`
	Tags              []string  `json:"tags"`
	ExecutionTime	  time.Time `json:"execution_time"`
	WatchguardStatus  string    `json:"watchguard_status"`
	WatchguardLog     []string  `json:"watchguard_log"`
	WatchguardVersion string    `json:"watchguard_version"`
	Comment 		  string    `json:"comment"`
}

func GetRoutes() map[string]func(*context.Ctx, *json.RawMessage) *context.Response {
	r := make(map[string]func(*context.Ctx, *json.RawMessage) *context.Response)

	r["getAll"] = GetAll
	r["get"] = Get
	r["download"] = Download
	r["search"] = Search

	return r
}

type GetParameters struct {
	Id string `json:"id"`
	ServiceName string `json:"service_name"`
	ObjectType string `json:"object_type"`
}

func GetAll(c *context.Ctx, parametersRaw *json.RawMessage) *context.Response {
	fmt.Println("GetAll results called")

	iter := c.C.Query(`SELECT id, sha256, source_id, service_name, object_type, service_version, execution_time FROM results`).Iter()
	fmt.Println("Found ? rows", iter.NumRows())
	var resultSlice []*Result
	if iter.NumRows() > 0 {
		for i := 0; i < iter.NumRows(); i++ {
			result := &Result{}
			err := iter.Scan(
				&result.Id,
				&result.SHA256,
				&result.SourceId,
				&result.ServiceName,
				&result.ObjectType,
				&result.ServiceVersion,
				&result.ExecutionTime,
			)
			if err != true {
				return &context.Response{Error: "Couldn't unmarshal row " + string(i)}
			}

			resultSlice = append(resultSlice, result)
		}
	}
	err := iter.Close()
	if err != nil {
		return &context.Response{Error: err.Error()}
	}

	return &context.Response{Result:  struct {
		Result []*Result
	}{
		resultSlice,
	},
	}
}

func Get(c *context.Ctx, parametersRaw *json.RawMessage) *context.Response {
	p := &GetParameters{}
	err := json.Unmarshal(*parametersRaw, p)
	if err != nil {
		return &context.Response{Error: err.Error()}
	}

	result := &Result{}

	uuid, err := gocql.ParseUUID(p.Id)
	if err != nil {
		return &context.Response{Error: err.Error()}
	}

	service_name := p.ServiceName
	object_type := p.ObjectType

	err = c.C.Query(`SELECT id, sha256, schema_version, user_id, source_id, source_tag, service_name, service_version, service_config, object_category, object_type, results, tags, watchguard_status, watchguard_log, watchguard_version FROM results WHERE service_name = ? AND object_type = ? AND id = ?`, service_name, object_type, uuid).Scan(
		&result.Id,
		&result.SHA256,
		&result.SchemaVersion,
		&result.UserId,
		&result.SourceId,
		&result.SourceTag,
		&result.ServiceName,
		&result.ServiceVersion,
		&result.ServiceConfig,
		&result.ObjectCategory,
		&result.ObjectType,
		&result.Results,
		&result.Tags,
		&result.WatchguardStatus,
		&result.WatchguardLog,
		&result.WatchguardVersion,
	)

	if err != nil {
		return &context.Response{Error: err.Error()}
	}

	r := bytes.NewReader(result.Results)
	archive, err := gzip.NewReader(r)
	if err != nil {
		return &context.Response{Error: err.Error()}
	}
	defer archive.Close()

	results_decompressed, err := ioutil.ReadAll(archive)
	if err != nil {
		return &context.Response{Error: err.Error()}
	}
	s := string(results_decompressed)
	s = strings.Replace(s, "\\n", "<br/>", -1)
	s = strings.Replace(s, "\\", "", -1)

	return &context.Response{Result: struct {
		Result *Result
		RawResults string
	}{
		result,
		s,
	},
	}
}

type DownloadParameters struct {
	Id string `json:"id"`
	ServiceName string `json:"service_name"`
	ObjectType string `json:"object_type"`
}

func Download(c *context.Ctx, parametersRaw *json.RawMessage) *context.Response {
	p := &DownloadParameters{}
	err := json.Unmarshal(*parametersRaw, p)
	if err != nil {
		return &context.Response{Error: err.Error()}
	}

	result := &Result{}

	uuid, err := gocql.ParseUUID(p.Id)
	if err != nil {
		return &context.Response{Error: err.Error()}
	}

	service_name := p.ServiceName
	object_type := p.ObjectType

	err = c.C.Query(`SELECT results FROM results WHERE service_name = ? AND object_type = ? AND id = ?`, service_name, object_type, uuid).Scan(
		&result.Results,
	)
	if err != nil {
		return &context.Response{Error: err.Error()}
	}

	r := bytes.NewReader(result.Results)
	archive, err := gzip.NewReader(r)
	if err != nil {
		return &context.Response{Error: err.Error()}
	}
	defer archive.Close()

	results_decompressed, err := ioutil.ReadAll(archive)
	if err != nil {
		return &context.Response{Error: err.Error()}
	}
	
	s := string(results_decompressed)

	s = strings.Replace(s, "\\n", "\\\n\\\r", -1)
	s = strings.Replace(s, "\\", "", -1)

	
	return &context.Response{Result: struct {
		Results string
	}{
		s,
	},
	}
}

type SearchParameters struct {
	SHA256        string
	ServiceName   string
	StartedStart  string
	StartedStop   string
	FinishedStart string
	FinishedStop  string
	Limit         string
	Filtering     string
}

func Search(c *context.Ctx, parametersRaw *json.RawMessage) *context.Response {
	p := &SearchParameters{}
	err := json.Unmarshal(*parametersRaw, p)
	if err != nil {
		return &context.Response{Error: err.Error()}
	}

	results := []*Result{}
	result := &Result{}

	var whereStmt []string
	var whereStmtValues []interface{}

	if p.SHA256 != "" {
		whereStmt = append(whereStmt, "sha256 = ?")
		whereStmtValues = append(whereStmtValues, p.SHA256)
	}

	if p.ServiceName != "" {
		whereStmt = append(whereStmt, "service_name = ?")
		whereStmtValues = append(whereStmtValues, p.ServiceName)
	}

	if p.StartedStart != "" {
		if t, err := time.Parse("2006-01-02 15:04:05", p.StartedStart); err == nil {
			whereStmt = append(whereStmt, "started_date_time > ?")
			whereStmtValues = append(whereStmtValues, t)
		} else {
			return &context.Response{Error: err.Error()}
		}
	}

	if p.StartedStop != "" {
		if t, err := time.Parse("2006-01-02 15:04:05", p.StartedStop); err == nil {
			whereStmt = append(whereStmt, "started_date_time < ?")
			whereStmtValues = append(whereStmtValues, t)
		} else {
			return &context.Response{Error: err.Error()}
		}
	}

	if p.FinishedStart != "" {
		if t, err := time.Parse("2006-01-02 15:04:05", p.FinishedStart); err == nil {
			whereStmt = append(whereStmt, "finished_date_time > ?")
			whereStmtValues = append(whereStmtValues, t)
		} else {
			return &context.Response{Error: err.Error()}
		}
	}

	if p.FinishedStop != "" {
		if t, err := time.Parse("2006-01-02 15:04:05", p.FinishedStop); err == nil {
			whereStmt = append(whereStmt, "finished_date_time < ?")
			whereStmtValues = append(whereStmtValues, t)
		} else {
			return &context.Response{Error: err.Error()}
		}
	}

	limit, err := strconv.Atoi(p.Limit)
	if limit == 0 || err != nil {
		limit = 100
	}

	where := ""
	if len(whereStmt) > 0 {
		where = " WHERE " + strings.Join(whereStmt, " AND ")
	}

	where += " LIMIT " + strconv.Itoa(limit)

	if p.Filtering == "on" {
		where += " ALLOW FILTERING"
	}

	// TODO: fix results, make everything lower case and revisit this statement
	q := c.C.Query("SELECT id, sha256, service_name, tags FROM results"+where, whereStmtValues...)

	iter := q.Iter()
	for iter.Scan(
		&result.Id,
		&result.SHA256,
		&result.ServiceName,
		&result.Tags,
	) {
		results = append(results, result)
		result = &Result{}
	}

	if err = iter.Close(); err != nil {
		return &context.Response{Error: err.Error()}
	}

	return &context.Response{Result: struct {
		Results []*Result
	}{
		results,
	},
	}
}
