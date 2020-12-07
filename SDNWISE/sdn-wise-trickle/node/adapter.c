 //
// Created by Hieu Nguyen on 3/11/2018.
//
#include "contiki.h"
#include <stdio.h>
#include "adapter.h"
#include "dev/serial-line.h"
#include "packet-handler.h"
#include "packet-buffer.h"

#define PRINTF(...) printf(__VA_ARGS__)

void to_controller(packet_t* p){
    /** Rearrange packet format
     * p: 1 16 0 1 0 3 2 98 0 1 2 255 1 0 2 83
     * Node -> Controller: 1 16 0.1 0.3 2 98 0.1 2 255 1 0 2 83
     * */
    int i;
    printf("Node -> Controller: ");
    printf("%d %d %d.%d %d.%d %d %d %d.%d ",
           p->header.net, p->header.len, p->header.dst.u8[0], p->header.dst.u8[1],
           p->header.src.u8[0], p->header.src.u8[1], (uint8_t)p->header.typ, p->header.ttl,
           p->header.nxh.u8[0], p->header.nxh.u8[1]);
    for (i=0; i < (p->header.len - PLD_INDEX); ++i){
        printf("%d ",get_payload_at(p,i));
    }
    printf("\n");
}

PROCESS(adapter_proc, "Adapter process" );
AUTOSTART_PROCESSES(&adapter_proc);
PROCESS_THREAD(adapter_proc, ev, data){
    PROCESS_BEGIN();
        while (1){
            PROCESS_WAIT_EVENT();
            if (ev == serial_line_event_message){
                int i, LEN;
                char *line;
                line = (char *) data;
                uint8_t *packet = (uint8_t *) line;
                LEN = packet[1] - 32;
                uint8_t packet_array[LEN];

                for (i = 0; i < LEN; i++){
                    packet_array[i] = packet[i] - (uint8_t)32;
                }
               scan_network(packet_array,node_t *);

                packet_t *p = get_packet_from_array(packet_array);
                printf("Received from embedded controller: ");
                for (i=0; i < LEN; i++){
                    printf("%d ", packet_array[i]);
                }
                printf("\n");
                p->info.rssi = 0;
                
                switch (p->header.typ){

                      case REQUEST:
           	     PRINTF("[PHD]: Data\n");
                     handle_resp(p);
                     break;

                    case OPEN_PATH:
                    PRINTF("[PHD]: Response\n");
                    handle_open(p);
                    break;
                    
                    default:
                    handle_packet(p);

			
                       }

                
            }
        }
    PROCESS_END();
}

void handle_resp(packet_t* p)
{
  int responsedst = p[3];
  printf("Sending response to destination");
  handle_p(p);
}

void handle_open(packet_t* p)
{
   int size = scan_network();
   int openPath[size];
   printf("Openpath paket send to destinations");
   handle_packet(p);
}

void scan_network(uint8_t nodes[], node_t *head)
{
   int node_id = nodes[3];
   int network_size = nodes[9];
   
   node_t *new_node;
 new_node = (node_t *) malloc(sizeof(node_t));
 new_node->data = node_id;
 new_node->next= head;
 head = new_node;
 printf("Node ID %d added to the Topology", node_id);
   
}



