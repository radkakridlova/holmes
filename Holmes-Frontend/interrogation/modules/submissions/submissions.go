package submissions

import (
	"encoding/json"
	"fmt"
	"github.com/gocql/gocql"
	"strconv"
	"strings"
	"time"

	"holmes-dp/Holmes-Frontend/interrogation/context"
)

type Submission struct {
	Id      string    `json:"id"`
	SHA256  string    `json:"sha256"`
	UserId  string    `json:"user_id"`
	Source  string    `json:"source"`
	DateTime    time.Time `json:"date_time"`
	ObjName string    `json:"obj_name"`
	Tags    []string  `json:"tags"`
	Comment string    `json:"comment"`
}

/*
id timeuuid,
        sha256 text,
        user_id text,
        source text,
        date_time timestamp,
        obj_name text,
        tags set<text>,
        comment text,
 */

func GetRoutes() map[string]func(*context.Ctx, *json.RawMessage) *context.Response {
	r := make(map[string]func(*context.Ctx, *json.RawMessage) *context.Response)

	r["getAll"] = GetAll
	r["get"] = Get
	r["search"] = Search

	return r
}

type GetParameters struct {
	Id string `json:"id"`
	Sha256 string `json:"sha256"`
}

func GetAll(c *context.Ctx, parametersRaw *json.RawMessage) *context.Response {
	fmt.Println("Getting all submissions")

	iter := c.C.Query(`SELECT id, sha256, user_id, source, date_time, obj_name FROM submissions`).Iter()

	fmt.Println("Found ? rows", iter.NumRows())
	var resultSlice []*Submission
	if iter.NumRows() > 0 {
		for i := 0; i < iter.NumRows(); i++ {
			submission := &Submission{}
			err := iter.Scan(
				&submission.Id,
				&submission.SHA256,
				&submission.UserId,
				&submission.Source,
				&submission.DateTime,
				&submission.ObjName,
			)
			if err != true {
				return &context.Response{Error: "Couldn't unmarshal row " + string(i)}
			}

			resultSlice = append(resultSlice, submission)
		}
	}
	err := iter.Close()
	if err != nil {
		return &context.Response{Error: err.Error()}
	}

	return &context.Response{Result:  struct {
		Submission []*Submission
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

	submission := &Submission{}

	sha256 := p.Sha256
	uuid, err := gocql.ParseUUID(p.Id)
	if err != nil {
		return &context.Response{Error: err.Error()}
	}


	type GetParameters struct {
		Id string `json:"id"`
		Sha256 string `json:"sha256"`
	}
	...
	err = c.C.Query(`SELECT id, sha256, user_id, source, date_time, obj_name, tags, comment FROM submissions WHERE id = ? AND sha256 = ? LIMIT 1`, uuid, sha256).Scan(
		&submission.Id,
		&submission.SHA256,
		&submission.UserId,
		&submission.Source,
		&submission.DateTime,
		&submission.ObjName,
		&submission.Tags,
		&submission.Comment,
	)

	if err != nil {
		return &context.Response{Error: err.Error()}
	}

	return &context.Response{Result: struct {
		Submission *Submission
	}{
		submission,
	},
	}
}

type SearchParameters struct {
	SHA256    string `json:"sha256"`
	ObjName   string `json:"obj_name"`
	Source    string `json:"source"`
	DateStart string `json:"date"`
	DateStop  string `json:"date"`
	Limit     string `json:"limit"`
	Filtering string `json:"filtering"`
}

func Search(c *context.Ctx, parametersRaw *json.RawMessage) *context.Response {
	p := &SearchParameters{}
	err := json.Unmarshal(*parametersRaw, p)
	if err != nil {
		return &context.Response{Error: err.Error()}
	}

	submissions := []*Submission{}
	submission := &Submission{}

	var whereStmt []string
	var whereStmtValues []interface{}

	if p.SHA256 != "" {
		whereStmt = append(whereStmt, "sha256 = ?")
		whereStmtValues = append(whereStmtValues, p.SHA256)
	}

	if p.ObjName != "" {
		whereStmt = append(whereStmt, "obj_name = ?")
		whereStmtValues = append(whereStmtValues, p.ObjName)
	}

	if p.Source != "" {
		whereStmt = append(whereStmt, "source = ?")
		whereStmtValues = append(whereStmtValues, p.Source)
	}

	if p.DateStart != "" {
		if t, err := time.Parse("2006-01-02 15:04:05", p.DateStart); err == nil {
			whereStmt = append(whereStmt, "date > ?")
			whereStmtValues = append(whereStmtValues, t)
		} else {
			return &context.Response{Error: err.Error()}
		}
	}

	if p.DateStop != "" {
		if t, err := time.Parse("2006-01-02 15:04:05", p.DateStop); err == nil {
			whereStmt = append(whereStmt, "date < ?")
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

	q := c.C.Query("SELECT id, sha256, obj_name, source, date FROM submissions"+where, whereStmtValues...)

	iter := q.Iter()
	for iter.Scan(
		&submission.Id,
		&submission.SHA256,
		&submission.ObjName,
		&submission.Source,
		&submission.DateTime,
	) {
		submissions = append(submissions, submission)
		submission = &Submission{}
	}

	if err = iter.Close(); err != nil {
		return &context.Response{Error: err.Error()}
	}

	return &context.Response{Result: struct {
		Submissions []*Submission
	}{
		submissions,
	},
	}
}
