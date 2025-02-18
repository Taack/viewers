<html>
<head>
    <style>
    body {
        margin: 0;
        overflow: hidden;
    }
    </style>
</head>

<body>
<div id="taackGlbViewer"></div>
<script type="module">
    console.log("tutut");
    import * as THREE from 'https://cdn.skypack.dev/three@0.134.0';
    import {OrbitControls} from 'https://cdn.skypack.dev/three@0.134.0/examples/jsm/controls/OrbitControls';
    import {GLTFLoader} from 'https://cdn.skypack.dev/three@0.134.0/examples/jsm/loaders/GLTFLoader.js';
    import {RGBELoader} from 'https://cdn.skypack.dev/three@0.134.0/examples/jsm/loaders/RGBELoader.js';
    import {RoughnessMipmapper} from 'https://cdn.skypack.dev/three@0.134.0/examples/jsm/utils/RoughnessMipmapper.js';

    console.log("Ok, let's Go !");
    let camera, scene, renderer, objects;
    init();
    render();

    // function onWindowResize() {
    //     console.log("AUO: onWindowResize")
    //     camera.aspect = window.innerWidth / window.innerHeight;
    //     camera.updateProjectionMatrix();
    //     renderer.setSize(window.innerWidth, window.innerHeight);
    //     render();
    // }

    function cameraAndLighting() {
//            const light = new THREE.AmbientLight( 0x404040 ); // soft white light
//            scene.add( light );
        camera = new THREE.PerspectiveCamera(45, window.innerWidth / window.innerHeight, 0.1, 100);
        camera.position.set(-0.5, 0, 0);
        camera.lookAt(0, 0, 0);
        // const pointLight = new THREE.PointLight(0x808080, 20, 500);
        // pointLight.position.set(0.25, 0, 0.433);
        // pointLight.castShadow = true;
        // camera.add(pointLight);
        scene.add(camera);
    }

    function initScene() {
        const worldScene = new THREE.Scene();

        worldScene.background = new THREE.Color(0x5d8eb9);
//             worldScene.fog = new THREE.Fog( 0x0000a0, 10, 22 );

//             const hemiLight = new THREE.HemisphereLight( 0xffffff, 0x444444 );
//             hemiLight.position.set( 0, 20, 0 );
//             worldScene.add( hemiLight );


        // ground
        const groundMesh = new THREE.Mesh(
            new THREE.PlaneGeometry(40, 40),
            new THREE.MeshPhongMaterial({
                color: 0x5d8eb9,
                depthWrite: false
            })
        );

        groundMesh.rotation.x = -Math.PI / 2;
        groundMesh.position.y = -0.1;
        groundMesh.receiveShadow = true;
        // worldScene.add(groundMesh);
        return worldScene;
    }

    function init() {
        console.log("AUO init taackGlbViewer..");
        const container = document.getElementById('taackGlbViewer');

        scene = initScene();

        new RGBELoader().load('/stp/hdr', function (texture) {
            texture.mapping = THREE.EquirectangularReflectionMapping;

            scene.background = texture;
            scene.environment = texture;

            render();
            const roughnessMipmapper = new RoughnessMipmapper(renderer);

            const loader = new GLTFLoader();

            console.log("Somewhere AUO2 ...");
            loader.load('/stp/stp3dFileContent?shaOne=${shaOne}', function (gltf) {
                console.log("AUO");
                objects = [];
                gltf.scene.traverse(function (mesh) {
                    var box = new THREE.Box3();
                    box.setFromObject(mesh);
//                       scene.add(new THREE.Box3Helper( box, 0xffff00 ));

                    if (mesh.isMesh) {
                        roughnessMipmapper.generateMipmaps(mesh.material);
                        objects.push(mesh);
                    }
                });
                scene.add(gltf.scene || gltf.scenes[0]);
                roughnessMipmapper.dispose();
                render();
            }, function (toto) {
                console.dir(toto);
            }, function (error) {
                console.error(error);
            });

        })
        renderer = new THREE.WebGLRenderer({antialias: true, alpha: true});
        renderer.setPixelRatio(window.devicePixelRatio);
        renderer.setSize(window.innerWidth, window.innerHeight);
        renderer.physicallyCorrectLights = true;
        renderer.outputEncoding = THREE.sRGBEncoding;
        renderer.setClearColor(0xcccccc);

        renderer.toneMapping = THREE.ACESFilmicToneMapping;
        renderer.toneMappingExposure = 1;
        renderer.outputEncoding = THREE.sRGBEncoding;
        container.appendChild(renderer.domElement);
        cameraAndLighting();

        const controls = new OrbitControls(camera, renderer.domElement);
        controls.addEventListener('change', render); // use if there is no animation loop
        controls.minDistance = 0.1;
        controls.maxDistance = 10;
        controls.target.set(0, 0, -0.2);
        controls.update();
        // window.addEventListener('resize', onWindowResize);
    }

    function render() {
        renderer.render(scene, camera);
    }
</script>
</body>
</html>
