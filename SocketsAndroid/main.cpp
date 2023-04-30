#include <iostream>
#include <cstring>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <unistd.h>

// Función para procesar los mensajes recibidos
void process_message(int socket_fd) {
    char buffer[1024] = {0};
    int valread = read(socket_fd, buffer, 1024);
    if (valread <= 0) {
        std::cout << "Cliente desconectado" << std::endl;
        close(socket_fd);
        return;
    }
    std::cout << "Mensaje recibido: " << buffer << std::endl;
    send(socket_fd, "Mensaje recibido", strlen("Mensaje recibido"), 0);
}

int main(int argc, char const *argv[]) {
    int server_fd, new_socket;
    struct sockaddr_in address;
    int opt = 1;
    int addrlen = sizeof(address);

    // Crear socket
    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0) {
        std::cerr << "Error al crear socket" << std::endl;
        return -1;
    }

    // Opción de socket para reutilizar la dirección
    if (setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt))) {
        std::cerr << "Error en setsockopt" << std::endl;
        return -1;
    }

    // Configurar dirección del socket
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = INADDR_ANY;
    address.sin_port = htons(5001);

    // Enlazar socket a la dirección y puerto especificados
    if (bind(server_fd, (struct sockaddr *)&address, sizeof(address)) < 0) {
        std::cerr << "Error en bind" << std::endl;
        return -1;
    }

    // Escuchar conexiones entrantes
    if (listen(server_fd, 3) < 0) {
        std::cerr << "Error en listen" << std::endl;
        return -1;
    }

    std::cout << "Servidor en espera de conexiones..." << std::endl;

    while (true) {
        // Aceptar nueva conexión
        if ((new_socket = accept(server_fd, (struct sockaddr *)&address, (socklen_t*)&addrlen)) < 0) {
            std::cerr << "Error en accept" << std::endl;
            return -1;
        }

        std::cout << "Nueva conexión aceptada" << std::endl;

        // Procesar mensajes entrantes
        process_message(new_socket);
    }

    return 0;
}
