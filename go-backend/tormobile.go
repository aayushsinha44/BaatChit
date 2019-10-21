package tormobile

import (
	"bytes"
	"context"
	"encoding/json"
	"net/http"
	"os"
	"strconv"
	"time"

	"github.com/cretz/bine/process"
	"github.com/cretz/bine/tor"
	"github.com/ipsn/go-libtor"
)

type LibTorWrapper struct{}

type MessageData struct {
	From    string `json:"from"`
	Message string `json:"message"`
}

type APIResponse struct {
	Status  string `json:"status"`
	Code    int    `json:"code"`
	Message string `json:"message"`
}

func (LibTorWrapper) New(ctx context.Context, args ...string) (process.Process, error) {
	return creator.New(ctx, args...)
}

var javaServerPort = 7893
var goServerPort = 6123

var creator = libtor.Creator
var t *tor.Tor
var onion *tor.OnionService
var httpClient *http.Client

func StartTorAndGetOnionUrl(dataDir string) string {

	var err error
	t, err = tor.Start(nil, &tor.StartConf{ProcessCreator: LibTorWrapper{}, DebugWriter: os.Stderr, DataDir: dataDir})
	if err != nil {
		return err.Error()
	}

	ctx, cancel := context.WithTimeout(context.Background(), 3*time.Minute)
	defer cancel()

	onion, err = t.Listen(ctx, &tor.ListenConf{RemotePorts: []int{80}, LocalPort: goServerPort})
	if err != nil {
		return err.Error()
	}

	return "http://" + onion.ID + ".onion/"
}

func StartServer() string {
	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")
		response := APIResponse{Status: "success", Code: 200, Message: ""}
		json.NewEncoder(w).Encode(response)
	})

	http.HandleFunc("/accept", func(w http.ResponseWriter, r *http.Request) {

		if r.Header.Get("from") == "" {
			response := APIResponse{Status: "failure", Code: 400, Message: "specify from"}
			json.NewEncoder(w).Encode(response)
		} else if r.Header.Get("message") == "" {
			response := APIResponse{Status: "failure", Code: 400, Message: "empty message not allowed"}
			json.NewEncoder(w).Encode(response)
		} else {

			url := "http://localhost:" + strconv.Itoa(javaServerPort)
			req, err := http.NewRequest("POST", url, bytes.NewBuffer([]byte("")))
			req.Header.Add("from", r.Header.Get("from"))
			req.Header.Add("message", r.Header.Get("message"))
			if err != nil {
				response := APIResponse{Status: "failure", Code: 400, Message: err.Error()}
				json.NewEncoder(w).Encode(response)
			}
			req.Header.Set("Content-Type", "application/json")

			client := &http.Client{Timeout: time.Minute}
			resp, err := client.Do(req)
			if err != nil {
				response := APIResponse{Status: "failure", Code: 400, Message: err.Error()}
				json.NewEncoder(w).Encode(response)
			}
			defer resp.Body.Close()

			var response APIResponse

			if err := json.NewDecoder(resp.Body).Decode(&response); err != nil {
				response := APIResponse{Status: "failure", Code: 400, Message: err.Error()}
				json.NewEncoder(w).Encode(response)
			}
			json.NewEncoder(w).Encode(response)
		}
	})

	go http.Serve(onion, nil)

	return "success"
}

func apiClient() (*http.Client, error) {

	if httpClient == nil {
		dialCtx, dialCancel := context.WithTimeout(context.Background(), time.Minute)
		defer dialCancel()
		dialer, err := t.Dialer(dialCtx, nil)
		if err != nil {
			// return err
			return nil, err
		}
		httpClient = &http.Client{Transport: &http.Transport{DialContext: dialer.DialContext}, Timeout: time.Minute}
	}

	return httpClient, nil
}

func TestOnionUrl(onionUrl string) string {

	client, err := apiClient()
	if err != nil {
		return err.Error()
	}
	resp, err := client.Get(onionUrl)
	if err != nil {
		return err.Error()
	}

	defer resp.Body.Close()

	var response APIResponse

	if err := json.NewDecoder(resp.Body).Decode(&response); err != nil {
		return err.Error()
	}

	return response.Status
}

func SendMessage(onionUrl string, message *MessageData) string {

	if message == nil {
		return "Nil message not allowed"
	}

	client, err := apiClient()
	if err != nil {
		return err.Error()
	}

	req, err := http.NewRequest("POST", onionUrl, bytes.NewBuffer([]byte("")))
	req.Header.Add("from", message.From)
	req.Header.Add("message", message.Message)
	// req, err := http.NewRequest("POST", onionUrl, strings.NewReader(data.Encode()))
	if err != nil {
		return err.Error()
	}
	req.Header.Set("Content-Type", "application/json")

	resp, err := client.Do(req)

	if err != nil {
		return err.Error()
	}

	defer resp.Body.Close()

	var response APIResponse

	if err := json.NewDecoder(resp.Body).Decode(&response); err != nil {
		return err.Error()
	}

	return response.Message
}

// func main() {
// 	t1 := time.Now()
// 	onionUrlVal := StartTorAndGetOnionUrl("data-dir")
// 	fmt.Println("=======================================")
// 	t2 := time.Now()
// 	diff := t2.Sub(t1)
// 	fmt.Println("Time: ", diff)
// 	fmt.Println(onionUrlVal)
// 	fmt.Println("=======================================")
// 	t1 = time.Now()
// 	serverStatus := StartServer()
// 	fmt.Println("=======================================")
// 	t2 = time.Now()
// 	diff = t2.Sub(t1)
// 	fmt.Println("Time: ", diff)
// 	fmt.Println("Server Status: ", serverStatus)
// 	fmt.Println("=======================================")
// 	// t1 = time.Now()
// 	// // testStatus := TestOnionUrl("https://www.facebookcorewwwi.onion/")
// 	// testStatus := TestOnionUrl(onionUrlVal)
// 	// fmt.Println("=======================================")
// 	// t2 = time.Now()
// 	// diff = t2.Sub(t1)
// 	// fmt.Println("Time: ", diff)
// 	// fmt.Println("Onion Test Status: ", testStatus)
// 	// fmt.Println("=======================================")
// 	t1 = time.Now()
// 	message := &MessageData{From: "aayush", Message: "hi there!"}
// 	status := SendMessage(onionUrlVal+"accept", message)
// 	fmt.Println("=======================================")
// 	t2 = time.Now()
// 	diff = t2.Sub(t1)
// 	fmt.Println("Time: ", diff)
// 	fmt.Println("Java Server Status: ", status)
// 	fmt.Println("=======================================")
// }
