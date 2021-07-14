'use strict'

const headerSingIn = document.getElementById("signIn");
const headerSignUp = document.getElementById("signUp");
const form = document.getElementById("form");
const login = new InputElement("text","login","","login","login",true,"fadeIn", "second");
const password = new InputElement('password', 'password', "", "password", 'password',true, 'fadeIn', 'third');
const email = new InputElement('email', 'email', "", 'email', 'email', false,'fadeIn', 'first', 'hidden');
const submitInput = new InputElement('submit', 'submit', 'Sign In', "", "", false,'fadeIn', 'fourth');
const loginError = new Paragraph('loginError', '', 'red', 'hidden');
const emailError = new Paragraph('emailError', '', 'red', 'hidden');
const passwordError = new Paragraph('passwordError', '', 'red', 'hidden');

email.renderAppend(form);
emailError.renderAppend(form);
login.renderAppend(form);
loginError.renderAppend(form);
password.renderAppend(form);
passwordError.renderAppend(form)
submitInput.renderAppend(form);

function showEmail() {
    form.setAttribute('action', '/registration');
    const inputEmail = email.inputElement;
    inputEmail.classList.remove('hidden');
    inputEmail.required = true;
    submitInput.inputElement.value = 'Sign up';
}

function hideEmail() {
    form.setAttribute('action', '/login');
    const inputEmail =  email.inputElement;
    inputEmail.classList.add('hidden');
    inputEmail.required = false;
    submitInput.inputElement.value = 'Sign in';
}

function changeActiveStatusClass(listActive, listNonActive) {
    listNonActive.add('active');
    listNonActive.remove('inactive', 'underlineHover');
    listActive.add('inactive', 'underlineHover');
    listActive.remove('active');
}

function changeActiveHeader() {
    headerSingIn.addEventListener('click', () => {
        changeActiveStatus(headerSignUp, headerSingIn);
        form.reset();
    });

    headerSignUp.addEventListener('click', () => {
        changeActiveStatus(headerSingIn, headerSignUp);
        form.reset();
    });

    function changeActiveStatus(activeElement, nonActiveElement) {
        if (nonActiveElement.id === 'signUp') {
                showEmail();
        } else {
                hideEmail();
        }
        changeActiveStatusClass(activeElement.classList, nonActiveElement.classList);
    }
}

changeActiveHeader();

//Server

function setErrorData(data) {
    for (let key in data) {
        if (key === 'login') {
            show('loginError', data[key]);
        } else if (key === 'email') {
            show('emailError', data[key]);
        } else {
            show('passwordError', data[key]);
        }
    }

    function show(selector, textContent) {
        const login = document.getElementById(selector);
        login.classList.remove('hidden');
        login.textContent = textContent;
    }
}

form.addEventListener('submit', (e) => {
    e.preventDefault();
    const url = form.getAttribute('action');
    if (url === '/registration') {
        let data = {};
        new FormData(form).forEach((item, key) => {
            data[key] = item;
        });
        fetch(url, {
            method: 'POST',
            headers: {
                'Content-type': 'application/json'
            },
            body: JSON.stringify(data)
        }).then(response =>
            response.text()
        ).then(body => {
            if (body === '') {
                hideEmail();
                changeActiveStatusClass(headerSignUp.classList, headerSingIn.classList);
            } else {
                setErrorData(JSON.parse(body));
            }
        });
    }else{
        let data = {};
        new FormData(form).forEach((item, key) => {
            if(!(key==='email'))
            data[key] = item;
        });
        fetch(url, {
            method: 'POST',
            headers: {
                'Content-type': 'application/json'
            },
            redirect: "follow",
            body: JSON.stringify(data)
        }).then(response => {
            if(response.redirected)
                document.location = response.url;
            return response.text();
        }).then(body => setErrorData(JSON.parse(body)));
    }
});

document.addEventListener('click',()=>{
    isParagraphHidden(emailError.pElement);
    isParagraphHidden(loginError.pElement);
    isParagraphHidden(passwordError.pElement);

    function isParagraphHidden(element){
        if(!element.classList.contains('hidden')){
            element.classList.add('hidden');
            element.textContent ='';
        }
    }
});


