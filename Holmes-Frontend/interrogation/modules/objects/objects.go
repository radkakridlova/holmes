package objects

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"strconv"
	"strings"

	"holmes-dp/Holmes-Frontend/interrogation/context"

	"github.com/aws/aws-sdk-go/service/s3"
)

type Object struct {
	SHA256      string   `json:"sha256"`
	SHA1        string   `json:"sha1"`
	MD5         string   `json:"md5"`
	MIME        string   `json:"mime"`
	ObjName     []string `json:"obj_name"`
	Submissions []string `json:"submissions"`
}
/*
Type             string    `json:"type"`
CreationDateTime time.Time `json:"creation_date_time"`
Submissions      []string  `json:"submissions"`
Source           []string  `json:"source"`

MD5    string `json:"md5"`
SHA1   string `json:"sha1"`
SHA256 string `json:"sha256"`

FileMime string   `json:"file_mime"`
FileName []string `json:"file_name"`

DomainFQDN      string `json:"domain_fqdn"`
DomainTLD       string `json:"domain_tld"`
DomainSubDomain string `json:"domain_sub_domain"`

IPAddress string `json:"ip_address"`
IPv6      bool   `json:"ip_v6"`

EmailAddress       string `json:"email_address"`
EmailLocalPart     string `json:"email_local_part"`
EmailDomainPart    string `json:"email_domain_part"`
EmailSubAddressing string `json:"email_sub_addressing"`

GenericIdentifier     string `json:"generic_identifier"`
GenericType           string `json:"generic_type"`
GenericDataRelAddress string `json:"generic_data_rel_address"`
*/

func GetRoutes() map[string]func(*context.Ctx, *json.RawMessage) *context.Response {
	r := make(map[string]func(*context.Ctx, *json.RawMessage) *context.Response)

	r["getAll"] = GetAll
	r["get"] = Get
	r["download"] = Download
	r["search"] = Search

	return r
}

type GetParameters struct {
	SHA256 string `json:"sha256"`
}

func GetAll(c *context.Ctx, parametersRaw *json.RawMessage) *context.Response {
	fmt.Print("GetAll objects called")

	iter := c.C.Query(`SELECT sha256, md5, file_mime, file_name, sha1, submissions FROM objects`).Iter()

	fmt.Println("Found ? rows", iter.NumRows())
	var resultSlice []*Object
	if iter.NumRows() > 0 {
		for i := 0; i < iter.NumRows(); i++ {
			object := &Object{}
			err := iter.Scan(
				&object.SHA256,
				&object.MD5,
				&object.MIME,
				&object.ObjName,
				&object.SHA1,
				&object.Submissions,
			)
			if err != true {
				return &context.Response{Error: "Couldn't unmarshal row " + string(i)}
			}

			resultSlice = append(resultSlice, object)
		}
	}
	err := iter.Close()
	if err != nil {
		return &context.Response{Error: err.Error()}
	}

	return &context.Response{Result:  struct {
		Object []*Object
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

	object := &Object{}

	err = c.C.Query(`SELECT sha256, md5, file_mime, file_name, sha1, submissions FROM objects WHERE sha256 = ?`, p.SHA256).Scan(
		&object.SHA256,
		&object.MD5,
		&object.MIME,
		&object.ObjName,
		&object.SHA1,
		&object.Submissions,
	)

	if err != nil {
		return &context.Response{Error: err.Error()}
	}

	return &context.Response{Result: struct {
		Object *Object
	}{
		object,
	},
	}
}

type DownloadParameters struct {
	SHA256 string `json:"sha256"`
}

func Download(c *context.Ctx, parametersRaw *json.RawMessage) *context.Response {
	p := &DownloadParameters{}
	err := json.Unmarshal(*parametersRaw, p)
	if err != nil {
		return &context.Response{Error: err.Error()}
	}

	resp, err := c.S3.GetObject(&s3.GetObjectInput{
		Bucket: &c.Bucket,
		Key:    &p.SHA256,
	})

	if err != nil {
		return &context.Response{Error: err.Error()}
	}

	objBytes, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return &context.Response{Error: err.Error()}
	}

	return &context.Response{Result: struct {
		Bytes []byte
	}{
		objBytes,
	},
	}
}

type SearchParameters struct {
	SHA256    string `json:"sha256"`
	MD5       string `json:"md5"`
	Hash      string `json:"hash"`
	MIME      string `json:"mime"`
	Limit     string `json:"limit"`
	Filtering string `json:"filtering"`
}

func Search(c *context.Ctx, parametersRaw *json.RawMessage) *context.Response {
	p := &SearchParameters{}
	err := json.Unmarshal(*parametersRaw, p)
	if err != nil {
		return &context.Response{Error: err.Error()}
	}

	objects := []*Object{}
	object := &Object{}

	var whereStmt []string
	var whereStmtValues []interface{}

	if len(p.Hash) == 64 {
		p.SHA256 = p.Hash
	} else if len(p.Hash) == 32 {
		p.MD5 = p.Hash
	}

	if p.SHA256 != "" {
		whereStmt = append(whereStmt, "sha256 = ?")
		whereStmtValues = append(whereStmtValues, p.SHA256)
	}

	if p.MD5 != "" {
		whereStmt = append(whereStmt, "md5 = ?")
		whereStmtValues = append(whereStmtValues, p.MD5)
	}

	if p.MIME != "" {
		whereStmt = append(whereStmt, "mime = ?")
		whereStmtValues = append(whereStmtValues, p.MIME)
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

	q := c.C.Query("SELECT sha256, md5, file_mime, file_name, sha1, submissions FROM objects"+where, whereStmtValues...)

	iter := q.Iter()
	for iter.Scan(
		&object.SHA256,
		&object.MD5,
		&object.MIME,
		&object.ObjName,
		&object.SHA1,
		&object.Submissions,
	) {
		objects = append(objects, object)
		object = &Object{}
	}

	if err = iter.Close(); err != nil {
		return &context.Response{Error: err.Error()}
	}

	return &context.Response{Result: struct {
		Objects []*Object
	}{
		objects,
	},
	}
}

func stringInSlice(a string, list []string) bool {
	for _, b := range list {
		if b == a {
			return true
		}
	}
	return false
}
