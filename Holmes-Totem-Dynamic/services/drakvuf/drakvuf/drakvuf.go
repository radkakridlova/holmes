package cuckoo

import (
	"bytes"
	"crypto/tls"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"io/ioutil"
	"mime/multipart"
	"net/http"
)

type Drakvuf struct {
	URL    	string
	Client 	*http.Client
}
/*
exmon
Formát KV_FORMAT32

Time=" FORMAT_TIMEVAL ",
RSP=%x,
ExceptionRecord=0x%x,
ExceptionCode=0x%x,
FirstChance=%d,
EIP=%x,
EAX=%x,
EBX=%x,
ECX=%x,
EDX=%x,
EDI=%x,
ESI=%x,
EBP=%x,
ESP=%x"

Formát KV_FORMAT64

Time=" FORMAT_TIMEVAL ",
RSP=%x,
ExceptionRecord=0x%x,
ExceptionCode=0x%x,
FirstChance=%d,
RIP=%x,
RAX=%x,
RBX=%x,
RCX=%x,
RDX=%x,
RSP=%x"
RBP=%x,
RSI=%x,
RDI=%x,
R8=%x,
R9=%x,
R10=%x,
R11=%x"
*/



type Status struct {
	Tasks     *StatusTasks     `json:"tasks"`
	Diskspace *StatusDiskspace `json:"diskspace"`
}

type StatusTasks struct {
	Running int `json:"running"`
	Pending int `json:"pending"`
}

type StatusDiskspace struct {
	Analyses *StatusSamples `json:"analyses"`
}

type StatusSamples struct {
	Total int `json:"total"`
	Free  int `json:"free"`
	Used  int `json:"used"`
}

type TasksCreateResp struct {
	TaskID int `json:"task_id"`
}

type TasksViewResp struct {
	Message string         `json:"message"`
	Task    *TasksViewTask `json:"task"`
}

type TasksViewTask struct {
	Status string `json:"status"`
}

type TasksReport struct {
	Info       *TasksReportInfo        `json:"info"`
	Signatures []*TasksReportSignature `json;"signatures"`
	Behavior   *TasksReportBehavior    `json:"behavior"`
}

type TasksReportInfo struct {
	Started float64         `json:"started"`
	Ended   float64         `json:"ended"`
	Id      int             `json:"id"`
	Machine json.RawMessage `json:"machine"` //can be TasksReportInfoMachine OR string
}

type TasksReportInfoMachine struct {
	Name string `json:"name"`
}

type TasksReportSignature struct {
	Severity    int    `json:"severity"`
	Description string `json:"description"`
	Name        string `json:"name"`
}

type TasksReportBehavior struct {
	Processes []*TasksReportBhvPcs   `json:"processes"`
	Summary   *TasksReportBhvSummary `json:"summary"`
}

type TasksReportBhvPcs struct {
	Name      string                   `json:"process_name"`
	Id        int                      `json:"process_id"`
	ParentId  int                      `json:"parent_id"`
	FirstSeen float64                  `json:"first_seen"`
	Calls     []*TasksReportBhvPcsCall `json:"calls"`
}

type TasksReportBhvPcsCall struct {
	Category  string          `json:"category"`
	Status    int             `json:"status"`
	Return    string          `json:"return"`
	Timestamp string          `json:"timestamp"`
	ThreadId  string          `json:"thread_id"`
	Repeated  int             `json:"repeated"`
	Api       string          `json:"api"`
	Arguments json.RawMessage `json:"arguments"`
	Id        int             `json:"id"`
}

type TasksReportBhvPcsCallArg struct {
	Name  string `json:"name"`
	Value string `json:"value"`
}

type TasksReportBhvSummary struct {
	Files   []string `json:"files"`
	Keys    []string `json:"keys"`
	Mutexes []string `json:"mutexes"`
}

type FilesView struct {
	Sample *FilesViewSample `json:"sample"`
}

type FilesViewSample struct {
	SHA1     string `json:"sha1"`
	FileType string `json:"file_type"`
	FileSize int    `json:"file_size"`
	CRC32    string `json:"crc32"`
	SSDeep   string `json:"ssdeep"`
	SHA256   string `json:"sha256"`
	SHA512   string `json:"sha512"`
	Id       int    `json:"id"`
	MD5      string `json:"md5"`
}



//DRAKVUF structs

/*bsodmon
"\"TimeStamp\" :" "\"" FORMAT_TIMEVAL "\","
"\"VCPU\": %" PRIu32 "," (uint32_t)
"\"CR3\": %" PRIu64 "," (uint64_t)
"\"ProcessName\": %s,"
"\"UserId\": %" PRIu64 ","
"\"PID\" : %d,"
"\"PPID\": %d,"
"\"BugCheckCode\": %" PRIu64 ","
"\"BugCheckName\": \"%s\","
"\"BugCheckParameter1\": %" PRIu64 ","
"\"BugCheckParameter2\": %" PRIu64 ","
"\"BugCheckParameter3\": %" PRIu64 ","
"\"BugCheckParameter4\": %" PRIu64*/
type Bsodmon struct {
	TimeStamp     string `json:"TimeStamp"`
	VCPU uint32 `json:"VCPU"`
	CR3 uint64    `json:"CR3"`
	ProcessName    string `json:"ProcessName"`
	UserId   uint64 `json:"UserId"`
	PID   int `json:"PID"`
	PPID   int `json:"PPID"`
	BugCheckCode       uint64    `json:"BugCheckCode"`
	BugCheckName      string `json:"BugCheckName"`
	BugCheckParameter1       uint64    `json:"BugCheckParameter1"`
	BugCheckParameter2      uint64 `json:"BugCheckParameter2"`
	BugCheckParameter3       uint64    `json:"BugCheckParameter3"`
	BugCheckParameter4      uint64 `json:"BugCheckParameter4"`
}


/*clipboardmon
"\"TimeStamp\" :" "\"" FORMAT_TIMEVAL "\","
"\"ProcessName\": %s,"
"\"UserName\": \"%s\","
"\"UserId\": %" PRIu64 ","
"\"PID\" : %d,"
"\"PPID\": %d,"
"\"Method\" : \"%s\","*/
type Clipboardmon struct {
	TimeStamp     string `json:"TimeStamp"`
	ProcessName    string `json:"ProcessName"`
	UserName   string `json:"UserName"`
	UserId   uint64 `json:"UserId"`
	PID   int `json:"PID"`
	PPID   int `json:"PPID"`
	Method     string `json:"Method"`
}


/*cpuidmon
"\"TimeStamp\" :" "\"" FORMAT_TIMEVAL "\","
"\"VCPU\": %" PRIu32 ","
"\"CR3\": %" PRIu64 ","
"\"ProcessName\": %s,"
"\"UserName\": \"%s\","
"\"UserId\": %" PRIu64 ","
"\"PID\" : %d,"
"\"PPID\": %d,"
"\"Leaf\": %" PRIu32 ","
"\"Subleaf\": %" PRIu32 ","
"\"RAX\": %" PRIu64 ","
"\"RBX\": %" PRIu64 ","
"\"RCX\": %" PRIu64 ","
"\"RDX\": %" PRIu64 ""*/
type Cpuidmon struct {
	TimeStamp     string `json:"TimeStamp"`
	VCPU uint32 `json:"VCPU"`
	CR3 uint64    `json:"CR3"`
	ProcessName    string `json:"ProcessName"`
	UserName   string `json:"UserName"`
	UserId   uint64 `json:"UserId"`
	PID   int `json:"PID"`
	PPID   int `json:"PPID"`
	Leaf     uint32 `json:"Leaf"`
	Subleaf uint32 `json:"Subleaf"`
	RAX uint64    `json:"RAX"`
	RBX    uint64 `json:"RBX"`
	RCX   uint64 `json:"RCX"`
	RDX   uint64 `json:"RDX"`
}


/*crashmon
"\"TimeStamp\" :" "\"" FORMAT_TIMEVAL "\","
"\"ProcessName\": %s,"
"\"UserId\": %" PRIu64 ","
"\"PID\" : %d,"
"\"PPID\": %d"*/
type Crashmon struct {
	TimeStamp     string `json:"TimeStamp"`
	ProcessName    string `json:"ProcessName"`
	UserId   uint64 `json:"UserId"`
	PID   int `json:"PID"`
	PPID   int `json:"PPID"`
}


/*debugmon
"\"TimeStamp\" :" "\"" FORMAT_TIMEVAL "\","
"\"VCPU\": %" PRIu32 ","
"\"CR3\": %" PRIu64 ","
"\"ProcessName\": %s,"
"\"UserName\": \"%s\","
"\"UserId\": %" PRIu64 ","
"\"PID\" : %d,"
"\"PPID\": %d,"
"\"RIP\" : %" PRIu64","
"\"DebugType\" : %" PRIi32 ","
"\"DebugTypeStr\": \"%s\""*/
type Debugmon struct {
	TimeStamp     string `json:"TimeStamp"`
	VCPU uint32 `json:"VCPU"`
	CR3 uint64    `json:"CR3"`
	ProcessName    string `json:"ProcessName"`
	UserName   string `json:"UserName"`
	UserId   uint64 `json:"UserId"`
	PID   int `json:"PID"`
	PPID   int `json:"PPID"`
	RIP     uint64 `json:"RIP"`
	DebugType uint32 `json:"DebugType"`
	DebugTypeStr string    `json:"DebugTypeStr"`
}


/*delaymon
"\"TimeStamp\" :" "\"" FORMAT_TIMEVAL "\","
"\"VCPU\": %" PRIu32 ","
"\"CR3\": %" PRIu64 ","
"\"ProcessName\": %s,"
"\"UserName\": \"%s\","
"\"UserId\": %" PRIu64 ","
"\"PID\" : %d,"
"\"PPID\": %d,"
"\"DelayIntervalMs\": %.4f"*/
type Delaymon struct {
	TimeStamp     string `json:"TimeStamp"`
	VCPU uint32 `json:"VCPU"`
	CR3 uint64    `json:"CR3"`
	ProcessName    string `json:"ProcessName"`
	UserName   string `json:"UserName"`
	UserId   uint64 `json:"UserId"`
	PID   int `json:"PID"`
	PPID   int `json:"PPID"`
	DelayIntervalMs     float32 `json:"DelayIntervalMs"`
}

/*dkommon
"\"TimeStamp\" :" "\"" FORMAT_TIMEVAL "\","
"\"ProcessName\": %s,"
"\"UserName\": \"%s\","
"\"UserId\": %" PRIu64 ","
"\"PID\" : %d,"
"\"PPID\": %d,"
"\"Message\": \"Hidden Process\","
"\"Message\": \"Zero Page Write\","
"\"Message\": \"%s\","
"\"DriverName\": \"%s\","*/
type Dkommon struct {
	TimeStamp     string `json:"TimeStamp"`
	ProcessName    string `json:"ProcessName"`
	UserName   string `json:"UserName"`
	UserId   uint64 `json:"UserId"`
	PID   int `json:"PID"`
	PPID   int `json:"PPID"`
	Message     []string `json:"Message"`
	DriverName string `json:"file_type"`
}


/*envmon
"\"TimeStamp\" :" "\"" FORMAT_TIMEVAL "\","
"\"ProcessName\": \"%s\","
"\"PID\" : %d,"
"\"PPID\": %d,"
"\"Method\" : \"%s\","
"\"ExtendedNameFormat\" : %lu,"
"\"ExtendedNameFormatStr\" : \"%s\""
"\"NameType\" : %lu,"
"\"NameTypeStr\" : \"%s\""
"\"Family\" : \"%s\","
"\"Flags\" : \"%s\""
"\"NetType\" : %lu"*/
type Envmon struct {
	TimeStamp     string `json:"TimeStamp"`
	ProcessName    string `json:"ProcessName"`
	PID   int `json:"PID"`
	PPID   int `json:"PPID"`
	Method     string `json:"Method"`
	ExtendedNameFormat int `json:"ExtendedNameFormat"`
	ExtendedNameFormatStr string    `json:"ExtendedNameFormatStr"`
	NameType    int `json:"NameType"`
	NameTypeStr   string `json:"NameTypeStr"`
	Family   string `json:"Family"`
	Flags   string `json:"Flags"`
	NetType       int    `json:"NetType"`
}


/*filedelete
"FileName: \"%s\"\n"
"FileSize: %zu\n"
"FileFlags: 0x%lx (%s)\n"
"SequenceNumber: %d\n"
"ControlArea: 0x%lx\n"
"PID: %" PRIu64 "\n"
"PPID: %" PRIu64 "\n"
"ProcessName: \"%s\"\n"
"\"TimeStamp\" :" "\"" FORMAT_TIMEVAL "\","
"\"ProcessName\": %s,"
"\"UserName\": \"%s\","
"\"UserId\": %" PRIu64 ","
"\"PID\" : %d,"
"\"PPID\": %d,"
"\"Method\" : \"%s\","
"\"FileName\" : %s,"
"\"Size\" : %ld,"
"\"Flags\" : %" PRIu64 ","
"\"FlagsExpanded\" : \"%s\""*/
type Filedelete struct {
	FileName     string `json:"FileName"`
	FileSize uint32 `json:"FileSize"`
	FileFlags uint64    `json:"FileFlags"`
	SequenceNumber    string `json:"SequenceNumber"`
	ControlArea   string `json:"ControlArea"`
	PID   int `json:"PID"`
	PPID   int `json:"PPID"`
	ProcessName    string `json:"ProcessName"`

	TimeStamp     string `json:"TimeStamp"`
	//ProcessName    string `json:"ProcessName"`
	UserName   string `json:"UserName"`
	UserId   uint64 `json:"UserId"`
	//PID   int `json:"PID"`
	//PPID   int `json:"PPID"`
	Method     string `json:"Method"`
	//FileName int `json:"FileName"`
	Size int    `json:"Size"`
	Flags    uint64 `json:"Flags"`
	FlagsExpanded   string `json:"FlagsExpanded"`
}


/*fileextractor
"\"TimeStamp\" :" "\"" FORMAT_TIMEVAL "\","
"\"ProcessName\": %s,"
"\"UserName\": \"%s\","
"\"UserId\": %" PRIu64 ","
"\"PID\" : %d,"
"\"PPID\": %d,"
"\"Method\" : \"%s\","
"\"FileName\" : %s,"
"\"Size\" : %ld,"
"\"Flags\" : %" PRIu64 ","
"\"FlagsExpanded\" : \"%s\""
"\"SeqNum\" : %d"*/
type Fileextractor struct {
	TimeStamp     string `json:"TimeStamp"`
	ProcessName    string `json:"ProcessName"`
	UserName   string `json:"UserName"`
	UserId   uint64 `json:"UserId"`
	PID   int `json:"PID"`
	PPID   int `json:"PPID"`
	Method     string `json:"Method"`
	FileName string `json:"FileName"`
	Size int    `json:"Size"`
	Flags    uint64 `json:"Flags"`
	FlagsExpanded   string `json:"FlagsExpanded"`
	SeqNum     int `json:"SeqNum"`
}


/*filetracer
"\"TimeStamp\" :" "\"" FORMAT_TIMEVAL "\","
"\"ProcessName\": %s,"
"\"UserName\": \"%s\","
"\"UserId\": %" PRIu64 ","
"\"PID\" : %d,"
"\"PPID\": %d,"
"\"Method\" : \"%s\","
"\"Operation\" : \"%s\","
"\"FileName\" : %s"
"\"SrcFileName\" : %s,"
"\"DstFileName\" : %s"
"\"DesiredAccess\": \"%s\","
"\"FileAttributes\": \"%s\","
"\"ShareAccess\": \"%s\","
"\"CreateDisposition\": \"%s\","
"\"CreateOptions\": \"%s\"",*/
type Filetracer struct {
	TimeStamp     string `json:"TimeStamp"`
	VCPU uint32 `json:"VCPU"`
	CR3 uint64    `json:"CR3"`
	ProcessName    string `json:"ProcessName"`
	UserName   string `json:"UserName"`
	UserId   uint64 `json:"UserId"`
	PID   int `json:"PID"`
	PPID   int `json:"PPID"`
	Method     string `json:"Method"`
	Operation string `json:"Operation"`
	FileName string `json:"FileName"`
	SrcFileName string `json:"SrcFileName"`
	DstFileName string `json:"DstFileName"`
	DesiredAccess string `json:"DesiredAccess"`
	FileAttributes string `json:"FileAttributes"`
	ShareAccess string `json:"ShareAccess"`
	CreateDisposition     string `json:"CreateDisposition"`
	CreateOptions string `json:"CreateOptions"`
}


/*librarymon
"\"TimeStamp\" :" "\"" FORMAT_TIMEVAL "\","
"\"PID\" : %d,"
"\"PPID\": %d,"
"\"ProcessName\": %s,"
"\"Method\" : \"%s\","
"\"ModuleName\" : \"%s\","
"\"ModulePath\" : \"%s\""*/
type Librarymon struct {
	TimeStamp     string `json:"TimeStamp"`
	PID   int `json:"PID"`
	PPID   int `json:"PPID"`
	ProcessName    string `json:"ProcessName"`
	Method     string `json:"Method"`
	ModuleName     string `json:"ModuleName"`
	ModulePath string `json:"ModulePath"`
}

/*memdump
"\"TimeStamp\":" "\"" FORMAT_TIMEVAL "\", "
"\"ProcessName\": %s, "
"\"UserName\": \"%s\", "
"\"UserId\": %" PRIu64 ", "
"\"PID\": %d, "
"\"PPID\": %d, "
"\"Method\": \"%s\", "
"\"DumpReason\": \"%s\", "
"\"DumpPID\": %d, "
"\"DumpAddr\": \"0x%" PRIx64 "\", "
"\"DumpSize\": \"0x%" PRIx64 "\", "
"\"DumpFilename\": %s",
"\"TargetPID\": %d, "
"\"WriteAddr\": \"0x%" PRIx64 "\"",*/
type Memdump struct {
	TimeStamp     string `json:"TimeStamp"`
	ProcessName    string `json:"ProcessName"`
	UserName   string `json:"UserName"`
	UserId   uint64 `json:"UserId"`
	PID   int `json:"PID"`
	PPID   int `json:"PPID"`
	Method     string `json:"Method"`
	DumpReason     string `json:"DumpReason"`
	DumpPID int `json:"DumpPID"`
	DumpAddr uint64    `json:"DumpAddr"`
	DumpSize    uint64 `json:"DumpSize"`
	DumpFilename   string `json:"DumpFilename"`
	TargetPID   int `json:"TargetPID"`
	WriteAddr   uint64 `json:"WriteAddr"`
}

/*objmon
"\"TimeStamp\" :" "\"" FORMAT_TIMEVAL "\","
"\"ProcessName\": %s,"
"\"UserId\": %" PRIu64 ","
"\"PID\" : %d,"
"\"PPID\": %d,"
"\"Key\" : \"%c%c%c%c\""*/
type Objmon struct {
	TimeStamp     string `json:"TimeStamp"`
	ProcessName    string `json:"ProcessName"`
	UserId   uint64 `json:"UserId"`
	PID   int `json:"PID"`
	PPID   int `json:"PPID"`
	Key     string `json:"Key"`
}


/*poolmon
"\"TimeStamp\" :" "\"" FORMAT_TIMEVAL "\","
"\"VCPU\": %" PRIu32 ","
"\"CR3\": %" PRIu64 ","
"\"ProcessName\": %s,"
"\"UserName\": \"%s\","
"\"UserId\": %" PRIu64 ","
"\"PID\" : %d,"
"\"PPID\": %d,"
"\"Tag\": \"%s\","
"\"PoolType\": \"%s\","
"\"Size\": %" PRIu64  ""*/
type Poolmon struct {
	TimeStamp     string `json:"TimeStamp"`
	VCPU uint32 `json:"VCPU"`
	CR3 uint64    `json:"CR3"`
	ProcessName    string `json:"ProcessName"`
	UserName   string `json:"UserName"`
	UserId   uint64 `json:"UserId"`
	PID   int `json:"PID"`
	PPID   int `json:"PPID"`

	Tag     string `json:"Tag"`
	PoolType string `json:"PoolType"`
	Size uint64    `json:"Size"`
}

/*procmon
"\"TimeStamp\" :" "\"" FORMAT_TIMEVAL "\","
"\"ProcessName\": %s,"
"\"UserName\": \"%s\","
"\"UserId\": %" PRIu64 ","
"\"PID\" : %d,"
"\"PPID\": %d,"
"\"Method\" : \"%s\","
"\"Status\" : %" PRIu64 ","
"\"NewPid\" : %d,"
"\"CmdLine\" : %s,"
"\"ImagePathName\" : %s,"
"\"DllPath\" : %s,"
"\"CurDir\" : %s"
"\"ExitStatus\" : %" PRIu64 ","
"\"ExitPid\" : %d"
"\"DesiredAccess\" : %" PRIu32 ","
"\"ObjectAttributes\" : %" PRIu64 ","
"\"ClientID\" : %d,"
"\"ClientName\": %s"
"\"ProcessHandle\" : %" PRIu64 ","
"\"NewProtectWin32\" : \"%s\""
"[PROCMON] TIME:" FORMAT_TIMEVAL " PROCESS PID:%u PPID:%u FILE:\"%s\"\n"*/
type Procmon struct {
	TimeStamp     string `json:"TimeStamp"`
	ProcessName    string `json:"ProcessName"`
	UserName   string `json:"UserName"`
	UserId   uint64 `json:"UserId"`
	PID   int `json:"PID"`
	PPID   int `json:"PPID"`
	Method     string `json:"Method"`
	Status     uint64 `json:"Status"`
	NewPid int `json:"NewPid"`
	CmdLine string    `json:"CmdLine"`
	ImagePathName    string `json:"ImagePathName"`
	DllPath   string `json:"DllPath"`
	CurDir   string `json:"CurDir"`
	ExitStatus   uint64 `json:"ExitStatus"`
	ExitPid       int    `json:"ExitPid"`
	DesiredAccess      uint32 `json:"DesiredAccess"`
	ObjectAttributes      uint64 `json:"ObjectAttributes"`
	ClientID      int `json:"ClientID"`
	ClientName      string `json:"ClientName"`
	ProcessHandle      uint64 `json:"ProcessHandle"`
	NewProtectWin32      string `json:"NewProtectWin32"`
	ProcmonTime      string `json:"[PROCMON] TIME"`
}

/*regmon
"\"TimeStamp\" :" "\"" FORMAT_TIMEVAL "\","
"\"ProcessName\": %s,"
"\"UserName\": \"%s\","
"\"UserId\": %" PRIu64 ","
"\"PID\" : %d,"
"\"PPID\": %d,"
"\"Method\" : \"%s\","
"\"Key\" : %s",*/
type Regmon struct {
	TimeStamp     string `json:"TimeStamp"`
	ProcessName    string `json:"ProcessName"`
	UserName   string `json:"UserName"`
	UserId   uint64 `json:"UserId"`
	PID   int `json:"PID"`
	PPID   int `json:"PPID"`
	Method     string `json:"Method"`
	Key string `json:"Key"`
}

/*socketmon
"\"TimeStamp\" :" "\"" FORMAT_TIMEVAL "\","
"\"ProcessName\": \"%s\","
"\"UserName\": \"%s\","
"\"UserId\": %" PRIu64 ","
"\"PID\" : %d,"
"\"PPID\": %d,"
"\"Owner\": \"%s\","
"\"OwnerId\": %" PRIi64 ","
"\"OwnerPID\" : %d,"
"\"OwnerPPID\": %d,"
"\"Protocol\": \"%s\","
"\"LocalIp\": \"%s\","
"\"LocalPort\": %d"
"\"TcpState\": \"%s\","
"\"RemoteIp\": \"%s\","
"\"RemotePort\": %d"
"\"ListenerIp\": \"%s\","
"\"ListenerPort\": %d"
"\"DnsName\": \"%s\","
"\"Method\": \"%s\""*/
type Socketmon struct {
	TimeStamp     string `json:"TimeStamp"`
	VCPU uint32 `json:"VCPU"`
	CR3 uint64    `json:"CR3"`
	ProcessName    string `json:"ProcessName"`
	UserName   string `json:"UserName"`
	UserId   uint64 `json:"UserId"`
	PID   int `json:"PID"`
	PPID   int `json:"PPID"`
	Owner     string `json:"Owner"`
	OwnerId int64 `json:"OwnerId"`
	OwnerPID int    `json:"OwnerPID"`
	OwnerPPID int    `json:"OwnerPPID"`
	Protocol string    `json:"Protocol"`
	LocalIp string    `json:"LocalIp"`
	LocalPort int    `json:"LocalPort"`
	TcpState string    `json:"TcpState"`
	RemoteIp string    `json:"RemoteIp"`
	RemotePort int    `json:"RemotePort"`
	ListenerIp string    `json:"ListenerIp"`
	ListenerPort int    `json:"ListenerPort"`
	DnsName string    `json:"DnsName"`
	Method string    `json:"Method"`
}



/*ssdtmon
"\"TimeStamp\" :" "\"" FORMAT_TIMEVAL "\","
"\"ProcessName\": %s,"
"\"UserName\": \"%s\","
"\"UserId\": %" PRIu64 ","
"\"PID\" : %d,"
"\"PPID\": %d,"
"\"TableIndex\": %" PRIi64*/
type Ssdtmon struct {
	TimeStamp     string `json:"TimeStamp"`
	ProcessName    string `json:"ProcessName"`
	UserName   string `json:"UserName"`
	UserId   uint64 `json:"UserId"`
	PID   int `json:"PID"`
	PPID   int `json:"PPID"`
	TableIndex     int64 `json:"TableIndex"`
}


/*syscalls
"\"TimeStamp\" :" "\"" FORMAT_TIMEVAL "\","
"\"VCPU\": %" PRIu32 ","
"\"CR3\": %" PRIu64 ","
"\"ProcessName\": %s,"
"\"UserName\": \"%s\","
"\"UserId\": %" PRIu64 ","
"\"PID\" : %d,"
"\"PPID\": %d,"
"\"Module\": \"%s\","
"\"Method\": \"%s\","
"\"Args\" Arguments: %" PRIu32 "\n"*/
type Syscalls struct {
	TimeStamp     string `json:"TimeStamp"`
	VCPU uint32 `json:"VCPU"`
	CR3 uint64    `json:"CR3"`
	ProcessName    string `json:"ProcessName"`
	UserName   string `json:"UserName"`
	UserId   uint64 `json:"UserId"`
	PID   int `json:"PID"`
	PPID   int `json:"PPID"`
	Module     string `json:"Module"`
	Method string `json:"Method"`
	Args uint32    `json:"Args"`
}

/*windowmon
"\"TimeStamp\" :" "\"" FORMAT_TIMEVAL "\","
"\"ProcessName\": %s,"
"\"UserName\": \"%s\","
"\"UserId\": %" PRIu64 ","
"\"PID\" : %d,"
"\"PPID\": %d,"
"\"Method\" : \"%s\","
"\"Class\" : %s,"
"\"Name\" : %s,"*/
type Windowmon struct {
	TimeStamp     string `json:"TimeStamp"`
	ProcessName    string `json:"ProcessName"`
	UserName   string `json:"UserName"`
	UserId   uint64 `json:"UserId"`
	PID   int `json:"PID"`
	PPID   int `json:"PPID"`
	Method     string `json:"Method"`
	Class string `json:"Class"`
	Name string    `json:"Name"`
}


/*wmimon
"\"TimeStamp\" :" "\"" FORMAT_TIMEVAL "\","
"\"ProcessName\": \"%s\","
"\"PID\" : %d,"
"\"PPID\": %d,"
"\"Method\" : \"%s\","
"\"Object\": \"%s\", "
"\"Function\": \"%s\","*/
type Wmimon struct {
	TimeStamp     string `json:"TimeStamp"`
	ProcessName    string `json:"ProcessName"`
	PID   int `json:"PID"`
	PPID   int `json:"PPID"`
	Method     string `json:"Method"`
	Object string `json:"Object"`
	Function string    `json:"Function"`
}


func New(URL string, verifySSL bool) (*Drakvuf, error) {
	tr := &http.Transport{}
	if !verifySSL {
		tr = &http.Transport{
			TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
		}
	}

	return &Drakvuf{
		URL:    URL,
		Client: &http.Client{Transport: tr},
	}, nil
}

func (c *Drakvuf) GetStatus() (*Status, error) {
	r := &Status{}
	resp, status, err := c.fastGet("/list", r)
	if err != nil || status != 200 {
		if err == nil {
			err = errors.New("no-200 ret")
		}

		if resp != nil {
			err = errors.New(fmt.Sprintf("%s -> [%d] %s", err.Error(), status, resp))
		}

		return nil, err
	}

	return r, nil
}

// submitTask submits a new task to the cuckoo api.
func (c *Drakvuf) NewTask(fileBytes []byte, fileName string, params map[string]string) (int, error) {
	// add the file to the request
	body := new(bytes.Buffer)
	writer := multipart.NewWriter(body)
	part, err := writer.CreateFormFile("file", fileName)
	if err != nil {
		return 0, err
	}
	part.Write(fileBytes)

	// add the extra payload to the request
	for key, val := range params {
		err = writer.WriteField(key, val)
		if err != nil {
			return 0, err
		}
	}

	err = writer.Close()
	if err != nil {
		return 0, err
	}

	// finalize request
	request, err := http.NewRequest("POST", c.URL+"/upload", body)
	if err != nil {
		return 0, err
	}
	request.Header.Add("Content-Type", writer.FormDataContentType())

	// perform request
	resp, err := c.Client.Do(request)
	if err != nil {
		return 0, err
	}
	defer resp.Body.Close()

	if resp.StatusCode != 200 {
		return 0, errors.New(resp.Status)
	}

	// parse response
	respBody, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return 0, err
	}

	r := &TasksCreateResp{}
	if err := json.Unmarshal(respBody, r); err != nil {
		return 0, err
	}

	return r.TaskID, nil
}

func (c *Drakvuf) TaskStatus(id int) (string, error) {
	r := &TasksViewResp{}
	resp, status, err := c.fastGet(fmt.Sprintf("/status/%d", id), r)
	if err != nil || status != 200 {
		if err == nil {
			err = errors.New("no-200 ret")
		}

		if resp != nil {
			err = errors.New(fmt.Sprintf("%s -> [%d] %s", err.Error(), status, resp))
		}

		return "", err
	}

	if r.Message != "" {
		return "", errors.New(r.Message)
	}

	return r.Task.Status, nil
}

func (c *Drakvuf) TaskReport(id int) (*TasksReport, error) {
	r := &TasksReport{}
	// @app.route("/logs/<task_uid>/<log_type>")
	resp, status, err := c.fastGet(fmt.Sprintf("/logs/%d/drakvuf", id), r)
	if err != nil || status != 200 {
		if err == nil {
			err = errors.New("no-200 ret")
		}

		if resp != nil {
			err = errors.New(fmt.Sprintf("%s -> [%d] %s", err.Error(), status, resp))
		}

		return nil, err
	}

	return r, nil
}

// not implemented
func (c *Drakvuf) GetFileInfoByMD5(md5 string) (*FilesViewSample, error) {
	r := &FilesView{}
	resp, status, err := c.fastGet("/files/view/md5/"+md5, r)
	if err != nil || status != 200 {
		if err == nil {
			err = errors.New("no-200 ret")
		}

		if resp != nil {
			err = errors.New(fmt.Sprintf("%s -> [%d] %s", err.Error(), status, resp))
		}

		return nil, err
	}

	return r.Sample, nil
}

// not implemented
func (c *Drakvuf) GetFileInfoByID(id string) (*FilesViewSample, error) {
	r := &FilesView{}
	resp, status, err := c.fastGet("/files/view/id/"+id, r)
	if err != nil || status != 200 {
		if err == nil {
			err = errors.New("no-200 ret")
		}

		if resp != nil {
			err = errors.New(fmt.Sprintf("%s -> [%d] %s", err.Error(), status, resp))
		}

		return nil, err
	}

	return r.Sample, nil
}

// Not implemented
func (c *Drakvuf) DeleteTask(id int) error {
	resp, status, err := c.fastGet(fmt.Sprintf("/tasks/delete/%d", id), nil)
	if err != nil {
		if resp != nil {
			err = errors.New(fmt.Sprintf("%s -> [%d] %s", err.Error(), status, resp))
		}

		return err
	}

	if status != 200 {
		return errors.New(fmt.Sprintf("%d - Response code not 200", status))
	}

	return nil
}

// FastGet is a wrapper for http.Get which returns only
// the important data from the request and makes sure
// everyting is closed properly.
func (c *Drakvuf) fastGet(url string, structPointer interface{}) ([]byte, int, error) {
	request, err := http.NewRequest("GET", c.URL + url, nil)
	if err != nil {
	    return nil, 0, err
	}

	resp, err := c.Client.Do(request)
	if err != nil {
		return nil, 0, err
	}
	defer safeResponseClose(resp)

	respBody, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return nil, 0, err
	}

	if structPointer != nil {
		err = json.Unmarshal(respBody, structPointer)
	}

	return respBody, resp.StatusCode, err
}

func safeResponseClose(r *http.Response) {
	if r == nil {
		return
	}

	io.Copy(ioutil.Discard, r.Body)
	r.Body.Close()
}
