const firebase = require('firebase-admin');
const hashmap = require('HashMap');

var serviceAccountProd = require("./AwesomeApp-d9055d5af840.json"); 
var serviceAccountDev = require("./awesomeappdev-82a28-firebase-adminsdk-2kvit-fce67ceec6.json"); 

const prodAdmin = firebase.initializeApp({
  credential: firebase.credential.cert(serviceAccountProd)
});

const devAdmin = firebase.initializeApp({
  credential: firebase.credential.cert(serviceAccountDev)
}, "dev");

var prod = prodAdmin.firestore();
var dev = devAdmin.firestore();

var projectsTabs = new hashmap.HashMap();
var usersProjectsTabs = new hashmap.HashMap();

const checkProjects = (cibleDB) => {
  return cibleDB.collection('Projects').get()
    .then((data) => {
		let promises = [];
		data.forEach((doc) => {
		  const data = doc.data();
		  //console.log(data['name'] + " has " + data['nbUsers'] + " nbUsers.");
		  projectsTabs.set(data['name'],data['nbUsers']);
		  promises.push(
		    cibleDB.collection('Users').where("currentProject", "==" , data['name']).get().then((userData) => {
				usersProjectsTabs.set(data['name'], userData.size );
				return;
			})
		  )
		})
		return Promise.all(promises);
	});
}

var devPromise = (checkProjects(dev).then(() => {
  console.log('***********************************');
  console.log('dev');
  console.log('***********************************');
  projectsTabs.forEach((nbUsers, project) => {
	  if (nbUsers != usersProjectsTabs.get(project)) {
	    console.log(project + " => " + nbUsers + " , really => " + usersProjectsTabs.get(project));
	  }
  })
}));

devPromise.then( () => {
projectsTabs.clear();
usersProjectsTabs.clear();

checkProjects(prod).then(() => {
  console.log('***********************************');
  console.log('prod');
  console.log('***********************************');
  projectsTabs.forEach((nbUsers, project) => {
	  if (nbUsers != usersProjectsTabs.get(project)) {
	    console.log(project + " => " + nbUsers + " , really => " + usersProjectsTabs.get(project));
	  }
  })
});
});

