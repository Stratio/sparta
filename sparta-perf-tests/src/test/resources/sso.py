import json
import requests
import sys
from bs4 import BeautifulSoup
from http.cookiejar import Cookie, CookieJar
from requests.packages.urllib3.exceptions import InsecureRequestWarning
requests.packages.urllib3.disable_warnings(InsecureRequestWarning)

def login_in_dcos(url, username, password):
    """
    Function that simulates the login in DCOS flow with SSO to obtain a valid
    cookie that will be used to make requests to Marathon
    """
    # First request to mesos master to be redirected to gosec sso login
    # page and be given a session cookie
    r = requests.Session()

    try:
        first_response = r.get(url, verify=False)
    except Exception as e:
        return ""

    callback_url = first_response.url

    # Parse response body for hidden tags needed in the data of our login post request
    body = first_response.text
    parser = BeautifulSoup(body, "lxml")
    hidden_tags = [tag.attrs for tag in parser.find_all("input", type="hidden")]
    data = {tag['name']: tag['value'] for tag in hidden_tags
            if tag['name'] == 'lt' or tag['name'] == 'execution'}

    # Add the rest of needed fields and login credentials in the data of
    # our login post request and send it
    data.update(
        {'_eventId': 'submit',
         'submit': 'LOGIN',
         'username': username,
         'password': password
        }
    )

    try:
        login_response = r.post(callback_url, data=data, verify=False)
    except Exception as e:
        return ""

    # Obtain dcos cookie from response
    try:
        return login_response.history[-1].cookies
    except Exception as e:
        return ""

def main():
    cookie = login_in_dcos(sys.argv[1], sys.argv[2], sys.argv[3])
    #print("dcos-acs-info-cookie=" + dict(cookie)["dcos-acs-info-cookie"])
    if not cookie:
        print("")
    else:
   ###  finalcookie="dcos-acs-auth-cookie=" + dict(cookie)["dcos-acs-auth-cookie"] + "; dcos-acs-info-cookie=" + dict(cookie)["dcos-acs-info-cookie"]
        finalcookie="user=" + dict(cookie)["user"]
        print(finalcookie)

if __name__ == "__main__":
    main()
## Execution python3 sso.py https://stratiodatagovernance.com/dictionaryapi/v1/login admin 1234
